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

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public final class NetworkData<T> {

    public static <T> NetworkData<? extends T> newSuccess(T data) {
        if (data == null) throw new IllegalArgumentException("data == null");
        return new NetworkData<>(data, null);
    }

    public static <T> NetworkData<? extends T> newFailure(int code, String msg) {
        if (msg == null) throw new IllegalArgumentException("msg == null");
        return new NetworkData<>(null, new StateIOException(code, msg));
    }

    public static <T> NetworkData<? extends T> newFailure(StateIOException cause) {
        Utils.requireNonNull(cause, "cause == null");
        return new NetworkData<>(null, cause);
    }

    public final int code;
    public final String msg;
    public final T data;
    public final boolean isSuccess;
    public final StateIOException cause;

    private NetworkData(T data, StateIOException cause) {
        this.data = data;
        this.cause = cause;
        this.isSuccess = (this.data != null);
        if (isSuccess) {
            code = 200;
            msg = "ok";
        } else {
            code = cause.state;
            msg = cause.getMessage();
        }
    }

}
