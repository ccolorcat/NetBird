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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
final class RealCall implements Call {
    private final NetBird netBird;
    private final Request request;
    private final Connection connection;
    private final AtomicBoolean executed;
    private final AtomicBoolean canceled;

    RealCall(NetBird netBird, Request request) {
        this.netBird = netBird;
        this.request = request;
        this.connection = netBird.connection.clone();
        this.executed = new AtomicBoolean(false);
        this.canceled = new AtomicBoolean(false);
    }

    @Override
    public Request request() {
        return request;
    }

    @Override
    public Response execute() throws IOException {
        if (executed.getAndSet(true)) throw new IllegalStateException("Already executed");
        if (!netBird.dispatcher.executed(this)) throw HttpStatus.duplicateRequest();
        try {
            return getResponseWithInterceptorChain();
        } finally {
            netBird.dispatcher.finished(this);
        }
    }

    @Override
    public void enqueue(Callback callback) {
        if (executed.getAndSet(true)) throw new IllegalStateException("Already executed");
        callback.onStart();
        netBird.dispatcher.enqueue(new AsyncCall(callback));
    }

    private Response getResponseWithInterceptorChain() throws IOException {
        final List<Interceptor> headInterceptors = netBird.headInterceptors;
        final List<Interceptor> tailInterceptors = netBird.tailInterceptors;
        final int size = headInterceptors.size() + tailInterceptors.size() + 3;
        final List<Interceptor> interceptors = new ArrayList<>(size);
        interceptors.addAll(headInterceptors);
        interceptors.add(new BridgeInterceptor(netBird.baseUrl, netBird.headerManager));
        interceptors.addAll(tailInterceptors);
        interceptors.add(new GzipInterceptor(netBird.gzipEnabled));
        interceptors.add(new ConnectionInterceptor(netBird));
        final Interceptor.Chain chain = new RealInterceptorChain(interceptors, 0, request, connection);
        return chain.proceed(request);
    }

    @Override
    public void cancel() {
        canceled.set(true);
        connection.cancel();
    }

    @Override
    public boolean canceled() {
        return canceled.get();
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Call clone() {
        return new RealCall(netBird, request);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RealCall realCall = (RealCall) o;

        if (!request.equals(realCall.request)) return false;
        if (!executed.equals(realCall.executed)) return false;
        return canceled.equals(realCall.canceled);
    }

    @Override
    public int hashCode() {
        int result = request.hashCode();
        result = 31 * result + executed.hashCode();
        result = 31 * result + canceled.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "RealCall{" +
                "request=" + request +
                ", executed=" + executed +
                ", canceled=" + canceled +
                '}';
    }


    final class AsyncCall implements Runnable {
        private final Callback callback;

        Request request() {
            return RealCall.this.request;
        }

        RealCall get() {
            return RealCall.this;
        }

        Callback callback() {
            return callback;
        }

        private AsyncCall(Callback callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            int code = HttpStatus.CODE_CONNECT_ERROR;
            String msg = null;
            try {
                if (RealCall.this.canceled.get()) {
                    callback.onFailure(RealCall.this, HttpStatus.requestCanceled());
                } else {
                    final Response response = getResponseWithInterceptorChain();
                    code = response.code;
                    msg = response.msg;
                    callback.onResponse(RealCall.this, response);
                }
            } catch (IOException e) {
                Log.e(e);
                if (msg == null) {
                    msg = Utils.nullElse(e.getMessage(), HttpStatus.MSG_CONNECT_ERROR);
                } else {
                    msg = "Response msg = " + msg + "\n Exception detail = " + e.toString();
                }
                callback.onFailure(RealCall.this, new StateIOException(code, msg, e));
            } finally {
                callback.onFinish();
                netBird.dispatcher.finished(this);
                Utils.close(RealCall.this.connection);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AsyncCall asyncCall = (AsyncCall) o;

            if (!RealCall.this.equals(asyncCall.get())) return false;
            return callback.equals(asyncCall.callback);
        }

        @Override
        public int hashCode() {
            return RealCall.this.hashCode() + 31 * callback.hashCode();
        }

        @Override
        public String toString() {
            return "AsyncCall{" +
                    "call=" + RealCall.this +
                    ", callback=" + callback +
                    '}';
        }
    }
}
