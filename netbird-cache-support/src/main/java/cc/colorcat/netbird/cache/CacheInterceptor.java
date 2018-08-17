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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cc.colorcat.netbird.Headers;
import cc.colorcat.netbird.HttpStatus;
import cc.colorcat.netbird.Interceptor;
import cc.colorcat.netbird.MutableHeaders;
import cc.colorcat.netbird.Parameters;
import cc.colorcat.netbird.Request;
import cc.colorcat.netbird.Response;
import cc.colorcat.netbird.ResponseBody;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public class CacheInterceptor implements Interceptor {
    public static CacheInterceptor newCacheHeadInterceptor(File cacheDirectory, long cacheSize, List<String> ignoredQueryNames) {
        try {
            DiskCache cache = DiskCache.open(cacheDirectory, cacheSize);
            return new CacheInterceptor(cache, ignoredQueryNames);
        } catch (IOException e) {
            return null;
        }
    }

    protected final DiskCache diskCache;
    protected final List<String> ignoredQueryNames;
    protected final Response errorResponse = new Response.Builder()
            .responseCode(HttpStatus.CODE_CONNECT_ERROR)
            .responseMsg(HttpStatus.MSG_CONNECT_ERROR)
            .headers(Headers.ofWithIgnoreNull(Collections.<String, List<String>>emptyMap()))
            .build();


    protected CacheInterceptor(DiskCache diskCache, List<String> ignoredQueryNames) {
        this.diskCache = diskCache;
        this.ignoredQueryNames = new ArrayList<>(ignoredQueryNames);
    }

    @Override
    public final Response intercept(Chain chain) throws IOException {
        final Request request = chain.request();
        if (request.method().needBody()) {
            return chain.proceed(request);
        }

        final String stableKey = md5(createStableKey(request));
        if (forceLoadCached()) {
            return nullElse(loadCached(stableKey), errorResponse);
        }

        final long requestMaxAge = CacheControl.parseMaxAge(request.headers());
        if (requestMaxAge > 0L || requestMaxAge == CacheControl.MAX_AGE_FOREVER) {
            final Response response = loadCached(stableKey);
            if (response != null) {
                if (requestMaxAge == CacheControl.MAX_AGE_FOREVER) {
                    return response;
                }
                final long responseTime = CacheControl.parseSaveDate(response.headers());
                if (responseTime != CacheControl.TIME_INVALIDATE && System.currentTimeMillis() - responseTime < requestMaxAge) {
                    return response;
                }
            }
        }

        Response response;
        if ((response = chain.proceed(request)).responseCode() != 200 || response.responseBody() == null) {
            response = nullElse(loadCached(stableKey), errorResponse);
        } else {
            final ResponseBody body = response.responseBody();
            final byte[] content = body.bytes();
            saveResponseContent(stableKey, content);
            response = response.newBuilder()
                    .responseBody(ResponseBody.create(content, body.contentType(), body.charset()))
                    .replaceHeaderIfExists("Content-Length", Long.toString(content.length))
                    .build();
            saveResponseHeaders(stableKey, response.headers());
        }
        return response;
    }

    protected boolean forceLoadCached() {
        return false;
    }

    protected String createStableKey(Request request) {
        StringBuilder builder = new StringBuilder(request.url()).append(request.path());
        Parameters parameters = request.parameters();
        for (int i = 0, size = parameters.size(); i < size; ++i) {
            String name = parameters.name(i);
            if (!ignoredQueryNames.contains(name)) {
                builder.append(name).append(parameters.value(i));
            }
        }
        return builder.toString();
    }

    private Response loadCached(String stableKey) throws IOException {
        Response response = null;
        byte[] content = loadCachedResponseContent(stableKey);
        if (content != null) {
            Headers headers = loadCachedResponseHeaders(stableKey);
            response = new Response.Builder()
                    .responseCode(200)
                    .responseMsg("OK")
                    .responseBody(ResponseBody.create(content, headers.contentType(), headers.charset()))
                    .headers(headers)
                    .build();
        }
        return response;
    }

    private byte[] loadCachedResponseContent(String stableKey) throws IOException {
        byte[] content = null;
        DiskCache.Snapshot snapshot = diskCache.getSnapshot(getBodyKey(stableKey));
        InputStream input = snapshot.getInputStream();
        if (input != null) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                byte[] buffer = new byte[4096];
                for (int length = input.read(buffer); length != -1; length = input.read(buffer)) {
                    output.write(buffer, 0, length);
                }
                output.flush();
                content = output.toByteArray();
            } catch (IOException e) {
                snapshot.requireDelete();
                throw e;
            } finally {
                close(input, output);
            }
        }
        return content;
    }

    private Headers loadCachedResponseHeaders(String stableKey) throws IOException {
        MutableHeaders headers = MutableHeaders.create(12);
        DiskCache.Snapshot snapshot = diskCache.getSnapshot(getHeaderKey(stableKey));
        InputStream input = snapshot.getInputStream();
        if (input != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(input));
            try {
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    headers.addLine(line);
                }
            } catch (IOException e) {
                snapshot.requireDelete();
                throw e;
            } finally {
                close(input, br);
            }
        }
        return headers;
    }

    private void saveResponseContent(String stableKey, byte[] content) throws IOException {
        DiskCache.Snapshot snapshot = diskCache.getSnapshot(getBodyKey(stableKey));
        OutputStream output = snapshot.getOutputStream();
        if (output != null) {
            InputStream input = new ByteArrayInputStream(content);
            try {
                byte[] buffer = new byte[4096];
                for (int length = input.read(buffer); length != -1; length = input.read(buffer)) {
                    output.write(buffer, 0, length);
                }
                output.flush();
            } catch (IOException e) {
                snapshot.requireDelete();
                throw e;
            } finally {
                close(input, output);
            }
        }
    }

    private void saveResponseHeaders(String stableKey, Headers headers) throws IOException {
        if (headers.isEmpty()) return;
        MutableHeaders mh = headers.toMutableHeaders();
        mh.set(CacheControl.HEADER_NAME_CACHE_DATE, Long.toString(System.currentTimeMillis()));
        byte[] bytes = mh.toMultiLine().getBytes();
        DiskCache.Snapshot snapshot = diskCache.getSnapshot(getHeaderKey(stableKey));
        OutputStream output = snapshot.getOutputStream();
        if (output != null) {
            InputStream input = new ByteArrayInputStream(bytes);
            try {
                byte[] buffer = new byte[2048];
                for (int length = input.read(buffer); length != -1; length = input.read(buffer)) {
                    output.write(buffer, 0, length);
                }
                output.flush();
            } catch (IOException e) {
                snapshot.requireDelete();
                throw e;
            } finally {
                close(input, output);
            }
        }
    }

    private static void close(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException ignore) {
            }
        }
    }

    private static String getBodyKey(String stableKey) {
        return stableKey + "_body";
    }

    private static String getHeaderKey(String stableKey) {
        return stableKey + "_header";
    }

    private static <T> T nullElse(T t, T onNull) {
        return t != null ? t : onNull;
    }

    private static String md5(String original) {
        String result = original;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(original.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder(bytes.length << 1);
            for (byte b : bytes) {
                sb.append(Character.forDigit((b & 0xf0) >> 4, 16))
                        .append(Character.forDigit(b & 0x0f, 16));
            }
            result = sb.toString();
        } catch (NoSuchAlgorithmException ignore) {
        }
        return result;
    }
}
