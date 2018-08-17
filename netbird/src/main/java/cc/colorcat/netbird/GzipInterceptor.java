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
import java.util.zip.GZIPInputStream;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
final class GzipInterceptor implements Interceptor {
    private final boolean gzipEnabled;

    GzipInterceptor(boolean gzipEnabled) {
        this.gzipEnabled = gzipEnabled;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (!gzipEnabled) return chain.proceed(chain.request());
        boolean transparentGzip = false;
        final Request.Builder builder = chain.request().unfreeze().newBuilder();
        if (builder.headerValue("Accept-Encoding") == null && builder.headerValue("Range") == null) {
            transparentGzip = true;
            builder.addHeader("Accept-Encoding", "gzip");
        }

        Response response = chain.proceed(builder.build().freeze());
        if (transparentGzip && "gzip".equalsIgnoreCase(response.header("Content-Encoding"))) {
            final ResponseBody body = response.responseBody;
            if (body != null) {
                final InputStream newStream = new GZIPInputStream(body.stream());
                final Response.Builder newBuilder = response.newBuilder()
                        .removeHeader("Content-Encoding")
                        .removeHeader("Content-Length");
                final ResponseBody newBody = ResponseBody.create(newStream, newBuilder.headers());
                response = newBuilder.responseBody(newBody).build();
            }
        }
        return response;
    }
}
