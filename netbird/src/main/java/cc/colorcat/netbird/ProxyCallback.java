/*
 * Copyright 2019 cxx
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
 * Date: 2019-12-24
 * GitHub: https://github.com/ccolorcat
 */
final class ProxyCallback<T> implements Callback {
    static <T> ProxyCallback<T> create(Parser<? extends T> parser, Listener<? super T> listener) {
        Utils.requireNonNull(parser, "parser == null");
        Utils.requireNonNull(listener, "listener == null");
        return new ProxyCallback<>(parser, listener);
    }

    private final Parser<? extends T> parser;
    private final Listener<? super T> listener;
    private NetworkData<? extends T> networkData;

    private ProxyCallback(Parser<? extends T> parser, Listener<? super T> listener) {
        this.parser = parser;
        this.listener = listener;
    }

    @Override
    public void onStart() {
        if (listener != null) {
            if (Utils.isTargetThread()) {
                listener.onStart();
            } else {
                Utils.onTargetThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStart();
                    }
                });
            }
        }
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response.code == 200 && response.responseBody != null) {
            networkData = parser.parse(response);
        }
        if (networkData == null) {
            networkData = NetworkData.newFailure(response.code, response.msg);
        }
    }

    @Override
    public void onFailure(Call call, StateIOException cause) {
        networkData = NetworkData.newFailure(cause);
    }

    @Override
    public void onFinish() {
        if (listener != null) {
            if (Utils.isTargetThread()) {
                deliverData();
            } else {
                Utils.onTargetThread(new Runnable() {
                    @Override
                    public void run() {
                        deliverData();
                    }
                });
            }
        }
    }

    private void deliverData() {
        if (networkData.isSuccess) {
            listener.onSuccess(networkData.data);
        } else {
            listener.onFailure(networkData.cause);
        }
        listener.onFinish();
    }
}
