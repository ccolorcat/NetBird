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

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public abstract class ResponseBody implements Closeable {

    public abstract String contentType();

    public abstract long contentLength();

    public abstract Charset charset();

    public abstract InputStream stream();

    public final Reader reader() {
        final Charset charset = charset();
        return charset != null ? new InputStreamReader(stream(), charset) : new InputStreamReader(stream());
    }

    public final Reader reader(String charsetIfAbsent) {
        final Charset charset = Charset.forName(charsetIfAbsent);
        return reader(charset);
    }

    public final Reader reader(Charset ifAbsent) {
        Charset charset = charset();
        if (charset == null) charset = ifAbsent;
        return new InputStreamReader(stream(), charset);
    }

    public final String string() throws IOException {
        return Utils.justRead(reader());
    }

    public final String string(String charsetIfAbsent) throws IOException {
        final Charset charset = Charset.forName(charsetIfAbsent);
        return Utils.justRead(reader(charset));
    }

    public final String string(Charset ifAbsent) throws IOException {
        return Utils.justRead(reader(ifAbsent));
    }

    public final byte[] bytes() throws IOException {
        return Utils.justRead(stream());
    }

    @Override
    public void close() {
        Utils.close(stream());
    }


    public static ResponseBody create(String content, String contentType) {
        return create(content, contentType, null);
    }

    public static ResponseBody create(String content, String contentType, Charset charset) {
        final Charset c = charset != null ? charset : Utils.parseCharset(contentType, null);
        final byte[] bytes = c != null ? content.getBytes(c) : content.getBytes();
        return create(bytes, contentType, c);
    }

    public static ResponseBody create(byte[] bytes, String contentType) {
        return create(new ByteArrayInputStream(bytes), contentType, bytes.length, null);
    }

    public static ResponseBody create(byte[] bytes, String contentType, Charset charset) {
        return create(new ByteArrayInputStream(bytes), contentType, bytes.length, charset);
    }

    public static ResponseBody create(final InputStream input, final String contentType, final long contentLength) {
        return create(input, contentType, contentLength, null);
    }

    public static ResponseBody create(final InputStream input, final String contentType, final long contentLength, final Charset charset) {
        if (input == null) throw new IllegalArgumentException("input == null");
        if (contentLength < -1L) throw new IllegalArgumentException("contentLength < -1L");
        return new ResponseBody() {
            @Override
            public String contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return contentLength;
            }

            @Override
            public Charset charset() {
                return charset != null ? charset : Utils.parseCharset(contentType, null);
            }

            @Override
            public InputStream stream() {
                return input;
            }
        };
    }

    public static ResponseBody create(final InputStream input, final Headers headers) {
        if (input == null) throw new IllegalArgumentException("input == null");
        if (headers == null) throw new IllegalArgumentException("headers = null");
        return new ResponseBody() {
            @Override
            public String contentType() {
                return headers.contentType();
            }

            @Override
            public long contentLength() {
                return headers.contentLength();
            }

            @Override
            public Charset charset() {
                return headers.charset();
            }

            @Override
            public InputStream stream() {
                return input;
            }
        };
    }
}
