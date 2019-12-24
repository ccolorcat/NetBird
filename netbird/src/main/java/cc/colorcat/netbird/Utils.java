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

package cc.colorcat.netbird;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
final class Utils {
    static final Charset UTF8 = Charset.forName("UTF-8");

    static <T> List<T> immutableList(List<T> list) {
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    static <T> T requireNonNull(T t, String msg) {
        if (t == null) throw new NullPointerException(msg);
        return t;
    }

    static <T> T nullElse(T value, T other) {
        return value != null ? value : other;
    }

    static <T extends CharSequence> T emptyElse(T value, T other) {
        return isEmpty(value) ? other : value;
    }

    static <K, V> Entry<List<K>, List<V>> unzipWithIgnoreNull(Map<K, List<V>> multimap) {
        List<K> ks = new ArrayList<>();
        List<V> vs = new ArrayList<>();
        for (Map.Entry<K, List<V>> entry : multimap.entrySet()) {
            K k = entry.getKey();
            List<V> vList = entry.getValue();
            if (k == null || vList == null) continue;
            for (V v : vList) {
                if (v == null) continue;
                ks.add(k);
                vs.add(v);
            }
        }
        return new Entry<>(ks, vs);
    }

    static long quiteToLong(String number, long defaultValue) {
        if (isEmpty(number)) return defaultValue;
        try {
            return Long.parseLong(number);
        } catch (Exception e) {
            Log.e(e);
        }
        return defaultValue;
    }

    static boolean isEmpty(CharSequence text) {
        return text == null || text.length() == 0;
    }

    static Charset parseCharset(String contentType, Charset defaultValue) {
        if (isEmpty(contentType)) return defaultValue;
        String[] params = contentType.split(";");
        final int length = params.length;
        for (int i = 1; i < length; i++) {
            String[] pair = params[i].trim().split("=");
            if (pair.length == 2) {
                if (pair[0].equalsIgnoreCase("charset")) {
                    try {
                        return Charset.forName(pair[1]);
                    } catch (Exception ignore) {
                    }
                }
            }
        }
        return defaultValue;
    }

    static void justDump(InputStream input, OutputStream output) throws IOException {
        BufferedInputStream bis = buffered(input);
        BufferedOutputStream bos = buffered(output);
        byte[] buffer = new byte[8192];
        for (int length = bis.read(buffer); length != -1; length = bis.read(buffer)) {
            bos.write(buffer, 0, length);
        }
        bos.flush();
    }

    static BufferedInputStream buffered(InputStream input) {
        return input instanceof BufferedInputStream ? (BufferedInputStream) input : new BufferedInputStream(input);
    }

    static BufferedOutputStream buffered(OutputStream output) {
        return output instanceof BufferedOutputStream ? (BufferedOutputStream) output : new BufferedOutputStream(output);
    }

    static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.e(e);
            }
        }
    }

    static String smartEncode(String value) {
        try {
            String decodedValue = decode(value);
            if (!value.equals(decodedValue)) {
                return value;
            }
        } catch (Exception e) {
            Log.e(e);
        }
        return encode(value);
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    static RequestBody buildRequestBody(Parameters parameters, List<FileBody> fileBodies, String boundary) {
        if (parameters.isEmpty() && fileBodies.isEmpty()) {
            return null;
        }
        if (parameters.isEmpty() && fileBodies.size() == 1) {
            return fileBodies.get(0);
        }
        if (!parameters.isEmpty() && fileBodies.isEmpty()) {
            return FormBody.create(parameters, true);
        }
        return MultipartBody.create(FormBody.create(parameters, false), fileBodies, boundary);
    }

    static String checkedUrl(String url) {
        if (!url.toLowerCase().matches("^(http)(s)?://(\\S)+")) {
            throw new IllegalArgumentException("Bad url = " + url + ", the scheme must be http or https");
        }
        return url;
    }

    static String justRead(Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader br = new BufferedReader(reader);
        char[] buffer = new char[4096];
        for (int length = br.read(buffer); length != -1; length = br.read(buffer)) {
            builder.append(buffer, 0, length);
        }
        return builder.toString();
    }

    static byte[] justRead(InputStream input) throws IOException {
        BufferedInputStream bis = buffered(input);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        for (int length = bis.read(buffer); length != -1; length = bis.read(buffer)) {
            bos.write(buffer, 0, length);
        }
        bos.flush();
        return bos.toByteArray();
    }

    static ExecutorService defaultService(int corePoolSize) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                10,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(),
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    static boolean isTargetThread() {
        return Platform.get().scheduler().isTargetThread();
    }

    static void onTargetThread(Runnable runnable) {
        Platform.get().scheduler().onTargetThread(runnable);
    }
}
