/*
 * Copyright 2018 cxx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.colorcat.netbird.cache;


import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
@SuppressWarnings("unused")
public final class DiskCache {
    private static final String DIRTY_SUFFIX = ".tmp";
    private static final Pattern LEGAL_KEY_PATTERN = Pattern.compile("[a-z0-9_-]{1,64}");

    private final LinkedHashMap<String, Snapshot> map;
    private File directory;

    private long maxSize;
    private long size;
    private final ThreadPoolExecutor executor =
            new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    private final Callable<Void> cleanupCallable = new Callable<Void>() {
        @Override
        public Void call() throws Exception {
            synchronized (DiskCache.this) {
                trimToSize(maxSize);
                return null;
            }
        }
    };

    private DiskCache(File directory, long maxSize) {
        this.directory = directory;
        this.maxSize = maxSize;
        this.map = new LinkedHashMap<>(0, 0.75F, true);
    }

    public static DiskCache open(File directory, long maxSize) throws IOException {
        if (maxSize <= 0L) {
            throw new IllegalArgumentException("maxSize <= 0L");
        }
        if (directory.isFile()) {
            throw new IOException(directory + " is not a directory!");
        }
        synchronized (DiskCache.class) {
            File dir = new File(directory, "diskCache");
            if (dir.exists() || dir.mkdirs()) {
                DiskCache cache = new DiskCache(dir, maxSize);
                cache.cleanDirtyFile();
                cache.readSnapshots();
                cache.asyncTrimToSize();
                return cache;
            }
            throw new IOException("failed to create directory: " + dir);
        }
    }

    private void cleanDirtyFile() throws IOException {
        File[] dirty = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() && file.getName().endsWith(DIRTY_SUFFIX);
            }
        });
        assert dirty != null;
        deleteIfExists(dirty);
    }

    private void readSnapshots() throws IOException {
        File[] files = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile();
            }
        });
        assert files != null;
        List<File> list = Arrays.asList(files);
        Collections.sort(list, new FileComparator());
        for (int i = 0, size = list.size(); i < size; ++i) {
            File file = list.get(i);
            this.size += file.length();
            String name = file.getName();
            map.put(name, new Snapshot(name));
        }
    }

    public synchronized Snapshot getSnapshot(String key) {
        checkKey(key);
        Snapshot snapshot = map.get(key);
        if (snapshot == null) {
            snapshot = new Snapshot(key);
            map.put(key, snapshot);
        }
        return snapshot;
    }

    void clear() throws IOException {
        deleteContents(directory);
    }

    long maxSize() {
        return maxSize;
    }

    long size() {
        return size;
    }

    private void checkKey(String key) {
        Matcher matcher = LEGAL_KEY_PATTERN.matcher(key);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("keys must match regex [a-z0-9_-]{1,64}: \"" + key + "\"");
        }
    }

    private void completeWriteSnapshot(Snapshot snapshot, boolean success) throws IOException {
        try {
            File dirty = snapshot.getDirtyFile();
            File clean = snapshot.getCleanFile();
            if (success) {
                if (dirty.exists()) {
                    long oldLength = clean.length();
                    long newLength = dirty.length();
                    renameTo(dirty, clean, true);
                    size = size - oldLength + newLength;
//                    asyncTrimToSize();
                }
            } else {
                deleteIfExists(dirty);
            }
        } finally {
            snapshot.writing = false;
            snapshot.committed = false;
            snapshot.hasErrors = false;
            if (snapshot.requiredDelete) {
                deleteSnapshot(snapshot);
            }
            asyncTrimToSize();
        }
    }

    private void deleteSnapshot(Snapshot snapshot) throws IOException {
        File clean = snapshot.getCleanFile();
        if (clean.exists()) {
            long length = clean.length();
            deleteIfExists(clean);
            if (map.remove(snapshot.key) != null) {
                size -= length;
            }
        }
    }

    private void asyncTrimToSize() {
        if (size > maxSize) {
            executor.submit(cleanupCallable);
        }
    }

    private void trimToSize(long maxSize) throws IOException {
        Iterator<Map.Entry<String, Snapshot>> iterator = map.entrySet().iterator();
        while (size > maxSize && iterator.hasNext()) {
            Map.Entry<String, Snapshot> toEvict = iterator.next();
            Snapshot value = toEvict.getValue();
            if (value.readCount == 0 && !value.writing) {
                File clean = value.getCleanFile();
                long cleanLength = clean.length();
                deleteIfExists(clean);
                size -= cleanLength;
                iterator.remove();
            }
        }
    }


    public final class Snapshot {
        private String key;

        private int readCount = 0;

        private boolean writing = false;
        private boolean committed = false;
        private boolean hasErrors = false;

        private boolean requiredDelete = false;

        private Snapshot(String key) {
            this.key = key;
        }

        public InputStream getInputStream() {
            synchronized (DiskCache.this) {
                try {
                    ++readCount;
                    return new SnapshotInputStream(new FileInputStream(getCleanFile()));
                } catch (FileNotFoundException e) {
                    --readCount;
                    return null;
                }
            }
        }

        long getContentLength() {
            return getCleanFile().length();
        }

        long getLastModified() {
            return getCleanFile().lastModified();
        }

        public OutputStream getOutputStream() {
            synchronized (DiskCache.this) {
                if (!writing) {
                    try {
                        FileOutputStream fos = new FileOutputStream(getDirtyFile());
                        writing = true;
                        return new SnapshotOutputStream(fos);
                    } catch (FileNotFoundException e) {
                        writing = false;
                        throw new IllegalStateException(directory + " does not exist.");
                    }
                }
                return null;
            }
        }

        public void requireDelete() throws IOException {
            synchronized (DiskCache.this) {
                if (!requiredDelete) {
                    requiredDelete = true;
                    if (readCount == 0 && !writing) {
                        deleteSnapshot(this);
                    }
                }
            }
        }

        private void completeRead() throws IOException {
            synchronized (DiskCache.this) {
                --readCount;
                if (readCount < 0) {
                    throw new IllegalStateException("readCount < 0");
                }
                if (readCount == 0) {
                    if (writing) {
                        if (committed) {
                            completeWriteSnapshot(this, !hasErrors);
                        }
                    } else {
                        if (requiredDelete) {
                            deleteSnapshot(this);
                        }
                    }
                }
            }
        }

        private void commitWrite() throws IOException {
            synchronized (DiskCache.this) {
                if (writing && !committed) {
                    committed = true;
                    if (readCount == 0) {
                        completeWriteSnapshot(this, !hasErrors);
                    }
                } else {
                    throw new IllegalStateException("writing = " + writing + ", committed = " + committed);
                }
            }
        }

        private File getCleanFile() {
            return new File(directory, key);
        }

        private File getDirtyFile() {
            return new File(directory, key + DIRTY_SUFFIX);
        }

        @Override
        public String toString() {
            return "Snapshot{" +
                    "key='" + key + '\'' +
                    ", readCount=" + readCount +
                    ", writing=" + writing +
                    ", committed=" + committed +
                    ", hasErrors=" + hasErrors +
                    ", requiredDelete=" + requiredDelete +
                    '}';
        }

        private class SnapshotInputStream extends FilterInputStream {
            private boolean closed = false;

            private SnapshotInputStream(InputStream in) {
                super(in);
            }

            @Override
            public void close() throws IOException {
                if (!closed) {
                    closed = true;
                    try {
                        in.close();
                    } finally {
                        completeRead();
                    }
                }
            }
        }


        private class SnapshotOutputStream extends FilterOutputStream {
            private boolean closed = false;

            private SnapshotOutputStream(OutputStream out) {
                super(out);
            }

            @Override
            public void write(int oneByte) {
                try {
                    out.write(oneByte);
                } catch (IOException e) {
                    hasErrors = true;
                }
            }

            @Override
            public void write(byte[] buffer) {
                write(buffer, 0, buffer.length);
            }

            @Override
            public void write(byte[] buffer, int offset, int length) {
                try {
                    out.write(buffer, offset, length);
                } catch (IOException e) {
                    hasErrors = true;
                }
            }

            @Override
            public void flush() {
                try {
                    out.flush();
                } catch (IOException e) {
                    hasErrors = true;
                }
            }

            @Override
            public void close() {
                if (!closed) {
                    closed = true;
                    try {
                        out.close();
                    } catch (IOException e) {
                        hasErrors = true;
                    } finally {
                        try {
                            commitWrite();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private static class FileComparator implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            return Long.compare(f1.lastModified(), f2.lastModified());
        }
    }

    private static void deleteIfExists(File... files) throws IOException {
        for (File file : files) {
            deleteIfExists(file);
        }
    }

    private static void deleteIfExists(File file) throws IOException {
        if (file.exists() && !file.delete()) {
            throw new IOException("failed to delete file: " + file);
        }
    }

    private static void deleteContents(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) throw new IOException("not a readable directory: " + dir);
        for (File file : files) {
            if (file.isDirectory()) {
                deleteContents(file);
            }
            if (!file.delete()) {
                throw new IOException("failed to delete file: " + file);
            }
        }
    }

    private static void renameTo(File from, File to, boolean deleteDest) throws IOException {
        if (deleteDest) {
            deleteIfExists(to);
        }
        if (!from.renameTo(to)) {
            throw new IOException("failed to rename from " + from + " to " + to);
        }
    }
}
