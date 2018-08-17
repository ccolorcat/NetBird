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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public final class FileBody extends RequestBody {
    static FileBody create(String name, String contentType, File file, UploadListener listener) {
        if (name == null) throw new NullPointerException("name == null");
        if (contentType == null) throw new NullPointerException("contentType == null");
        if (file == null) throw new NullPointerException("file == null");
        if (!file.exists()) throw new IllegalArgumentException(file.getAbsolutePath() + " is not exists");
        return new FileBody(name, contentType, file, listener);
    }

    final String name;
    private final String contentType;
    final File file;
    private final UploadListener listener;

    private FileBody(String name, String contentType, File file, UploadListener listener) {
        this.name = name;
        this.contentType = contentType;
        this.file = file;
        this.listener = listener;
    }

    @Override
    public String contentType() {
        return contentType;
    }

    @Override
    public long contentLength() throws IOException {
        return file.length();
    }

    @Override
    public void writeTo(OutputStream output) throws IOException {
        InputStream input = Utils.buffered(ProgressInputStream.of(file, listener));
        try {
            Utils.justDump(input, output);
        } finally {
            Utils.close(input);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileBody fileBody = (FileBody) o;
        return Objects.equals(name, fileBody.name) &&
                Objects.equals(contentType, fileBody.contentType) &&
                Objects.equals(file, fileBody.file) &&
                Objects.equals(listener, fileBody.listener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, contentType, file, listener);
    }

    @Override
    public String toString() {
        return "FileBody{" +
                "name='" + name + '\'' +
                ", contentType='" + contentType + '\'' +
                ", file=" + file +
                ", listener=" + listener +
                '}';
    }
}
