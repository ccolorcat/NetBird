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

import java.util.List;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public final class Response {
    final int code;
    final String msg;
    final Headers headers;
    final ResponseBody responseBody;

    private Response(Builder builder) {
        this.code = builder.code;
        this.msg = builder.msg;
        this.headers = builder.headers.toHeaders();
        this.responseBody = builder.responseBody;
    }

    public int responseCode() {
        return code;
    }

    public String responseMsg() {
        return msg;
    }

    public Headers headers() {
        return headers;
    }

    public String header(String name) {
        return headers.value(name);
    }

    public String header(String name, String defaultValue) {
        return headers.value(name, defaultValue);
    }

    public ResponseBody responseBody() {
        return responseBody;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }


    public static final class Builder {
        private int code;
        private String msg;
        private MutableHeaders headers;
        private ResponseBody responseBody;

        public Builder() {
            this.code = HttpStatus.CODE_CONNECT_ERROR;
            this.msg = HttpStatus.MSG_CONNECT_ERROR;
            this.headers = MutableHeaders.create(16);
            this.responseBody = null;
        }

        private Builder(Response response) {
            this.code = response.code;
            this.msg = response.msg;
            this.headers = response.headers.toMutableHeaders();
            this.responseBody = response.responseBody;
        }

        public int responseCode() {
            return code;
        }

        public String responseMsg() {
            return msg;
        }

        public Headers headers() {
            return headers.toHeaders();
        }

        public ResponseBody responseBody() {
            return responseBody;
        }

        public Builder responseCode(int code) {
            this.code = code;
            return this;
        }

        public Builder responseMsg(String msg) {
            this.msg = msg;
            return this;
        }

        public Builder headers(Headers headers) {
            this.headers = headers.toMutableHeaders();
            return this;
        }

        public Builder addHeader(String name, String value) {
            this.headers.add(name, value);
            return this;
        }

        public Builder addHeaderIfNot(String name, String value) {
            this.headers.addIfNot(name, value);
            return this;
        }

        public Builder addHeaders(List<String> names, List<String> values) {
            this.headers.addAll(names, values);
            return this;
        }

        public Builder setHeader(String name, String value) {
            this.headers.set(name, value);
            return this;
        }

        public Builder replaceHeaderIfExists(String name, String value) {
            this.headers.replaceIfExists(name, value);
            return this;
        }

        public Builder removeHeader(String name) {
            this.headers.removeAll(name);
            return this;
        }

        public Builder clearHeaders() {
            this.headers.clear();
            return this;
        }

        public Builder responseBody(ResponseBody responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        public Response build() {
            return new Response(this);
        }
    }
}
