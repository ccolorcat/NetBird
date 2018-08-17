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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
final class BridgeInterceptor implements Interceptor {
    private final String baseUrl;
    private final HeaderManager manager;

    BridgeInterceptor(String baseUrl, HeaderManager manager) {
        this.baseUrl = baseUrl;
        this.manager = manager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request.Builder builder = chain.request().newBuilder();
        URI uri = URI.create(Utils.emptyElse(builder.url(), baseUrl));
        final String path = builder.path();
        if (!Utils.isEmpty(path)) uri = uri.resolve(path);
        String url = uri.toString();

        final RequestBody body;
        if (!builder.method().needBody()) {
            final String query = concatParameters(builder.names(), builder.values());
            if (query != null) {
                url = url + '?' + query;
                builder.clear();
            }
        } else if ((body = builder.requestBody()) != null) {
            builder.setHeader(Headers.CONTENT_TYPE, body.contentType());
            final long contentLength = body.contentLength();
            if (contentLength > 0L) {
                builder.setHeader(Headers.CONTENT_LENGTH, Long.toString(contentLength))
                        .removeHeader("Transfer-Encoding");
            } else {
                builder.setHeader("Transfer-Encoding", "chunked")
                        .removeHeader(Headers.CONTENT_LENGTH);
            }
        }
        Headers headers = manager.loadForRequest(url);
        if (!headers.isEmpty()) {
            for (NameAndValue nv : headers) {
                builder.addHeaderIfNot(nv.name, nv.value);
            }
        }
        builder.url(url).clearPath()
                .addHeaderIfNot("Host", uri.getHost())
                .addHeaderIfNot("Connection", "Keep-Alive")
                .addHeaderIfNot("User-Agent", Version.userAgent());

        Response response = chain.proceed(builder.build().freeze());
        if (manager != HeaderManager.EMPTY) {
            manager.saveFromResponse(url, response.headers);
        }
        final DownloadListener listener = builder.downloadListener();
        final ResponseBody responseBody = response.responseBody;
        if (listener != null && responseBody != null) {
            final long contentLength = responseBody.contentLength();
            if (contentLength > 0L) {
                final InputStream newStream = ProgressInputStream.of(responseBody.stream(), contentLength, listener);
                final ResponseBody newBody = ResponseBody.create(newStream, responseBody.contentType(), contentLength, responseBody.charset());
                response = response.newBuilder().responseBody(newBody).build();
            }
        }
        return response;
    }

    private static String concatParameters(List<String> names, List<String> values) {
        if (names.isEmpty()) return null;
        StringBuilder builder = new StringBuilder();
        for (int i = 0, size = names.size(); i < size; ++i) {
            if (i > 0) builder.append('&');
            String encodedName = Utils.smartEncode(names.get(i));
            String encodedValue = Utils.smartEncode(values.get(i));
            builder.append(encodedName).append('=').append(encodedValue);
        }
        return builder.toString();
    }
}
