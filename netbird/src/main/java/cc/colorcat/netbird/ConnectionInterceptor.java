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

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
final class ConnectionInterceptor implements Interceptor {
    private final NetBird netBird;

    ConnectionInterceptor(NetBird netBird) {
        this.netBird = netBird;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Connection conn = chain.connection();
        final Request request = chain.request();
        conn.connect(netBird, request);
        conn.writeHeaders(request.headers);
        final Method method = request.method;
        if (method.needBody()) {
            final RequestBody body = request.requestBody();
            if (body == null) {
                throw new IllegalArgumentException("method " + request.method.name() + " must have a request body");
            }
            conn.writeRequestBody(body);
        }
        final Headers headers = Utils.nullElse(conn.responseHeaders(), Headers.EMPTY);
        final int code = conn.responseCode();
        final String msg = Utils.nullElse(conn.responseMsg(), "");
        ResponseBody body = null;
        if (code == 200) {
            body = conn.responseBody(headers);
        }
        return new Response.Builder().responseCode(code).responseMsg(msg).headers(headers).responseBody(body).build();
    }
}
