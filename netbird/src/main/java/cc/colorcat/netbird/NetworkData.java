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

import java.util.Objects;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public final class NetworkData<T> {

    public static <T> NetworkData<? extends T> newSuccess(T data) {
        if (data == null) throw new IllegalArgumentException("data == null");
        return new NetworkData<>(200, "ok", data);
    }

    public static <T> NetworkData<? extends T> newFailure(int code, String msg) {
        if (msg == null) throw new IllegalArgumentException("msg == null");
        return new NetworkData<>(code, msg, null);
    }

    public final int code;
    public final String msg;
    public final T data;
    public final boolean isSuccess;

    private NetworkData(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.isSuccess = (this.data != null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkData<?> that = (NetworkData<?>) o;
        return code == that.code &&
                Objects.equals(msg, that.msg) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, msg, data);
    }

    @Override
    public String toString() {
        return "NetworkData{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                ", isSuccess=" + isSuccess +
                '}';
    }
}
