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
import java.util.List;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
final class RealInterceptorChain implements Interceptor.Chain {
    private final List<Interceptor> interceptors;
    private final int index;
    private final Request request;
    private final Connection connection;

    RealInterceptorChain(List<Interceptor> interceptors, int index, Request request, Connection connection) {
        this.interceptors = interceptors;
        this.index = index;
        this.request = request;
        this.connection = connection;
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public Request request() {
        return request;
    }

    @Override
    public Response proceed(Request request) throws IOException {
        RealInterceptorChain next = new RealInterceptorChain(interceptors, index + 1, request, connection);
        Interceptor interceptor = interceptors.get(index);
        return interceptor.intercept(next);
    }
}
