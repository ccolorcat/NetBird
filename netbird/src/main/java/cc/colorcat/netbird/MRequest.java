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

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public final class MRequest<T> extends Request {
    final Parser<? extends T> parser;
    final Listener<? super T> listener;

    private MRequest(Builder<T> builder) {
        super(builder);
        this.parser = builder.parser;
        this.listener = builder.listener;
    }

    public Parser<? extends T> parser() {
        return parser;
    }

    public Listener<? super T> listener() {
        return listener;
    }

    @Override
    public Builder<T> newBuilder() {
        if (freeze) throw new IllegalStateException("The request has been frozen, call isFreeze() to check");
        return new Builder<>(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MRequest<?> mRequest = (MRequest<?>) o;
        return Objects.equals(parser, mRequest.parser) &&
                Objects.equals(listener, mRequest.listener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parser, listener);
    }

    @Override
    public String toString() {
        return "MRequest{" +
                "url='" + url + '\'' +
                ", path='" + path + '\'' +
                ", method=" + method +
                ", parameters=" + parameters +
                ", fileBodies=" + fileBodies +
                ", headers=" + headers +
                ", downloadListener=" + downloadListener +
                ", boundary='" + boundary + '\'' +
                ", tag=" + tag +
                ", parser=" + parser +
                ", listener=" + listener +
                '}';
    }


    public interface Listener<R> {

        void onStart();

        void onSuccess(R result);

        void onFailure(int code, String msg);

        void onFinish();
    }

    public static abstract class SimpleListener<R> implements Listener<R> {
        @Override
        public void onStart() {
        }

        @Override
        public void onSuccess(R result) {
        }

        @Override
        public void onFailure(int code, String msg) {
        }

        @Override
        public void onFinish() {
        }
    }


    public static final class Builder<T> extends Request.Builder {
        private Parser<? extends T> parser;
        private Listener<? super T> listener;

        public Builder(Parser<? extends T> parser) {
            super();
            this.parser = parser;
        }

        private Builder(MRequest<T> request) {
            super(request);
            this.parser = request.parser;
            this.listener = request.listener;
        }

        public Builder<T> parser(Parser<? extends T> parser) {
            if (parser == null) throw new IllegalArgumentException("parser == null");
            this.parser = parser;
            return this;
        }

        public Builder<T> listener(Listener<? super T> listener) {
            this.listener = listener;
            return this;
        }

        @Override
        public Builder<T> url(String url) {
            super.url(url);
            return this;
        }

        @Override
        public Builder<T> clearUrl() {
            super.clearUrl();
            return this;
        }

        @Override
        public Builder<T> path(String path) {
            super.path(path);
            return this;
        }

        @Override
        public Builder<T> clearPath() {
            super.clearPath();
            return this;
        }

        @Override
        public Builder<T> method(Method method) {
            super.method(method);
            return this;
        }

        @Override
        public Builder<T> get() {
            super.get();
            return this;
        }

        @Override
        public Builder<T> head() {
            super.head();
            return this;
        }

        @Override
        public Builder<T> trace() {
            super.trace();
            return this;
        }

        @Override
        public Builder<T> options() {
            super.options();
            return this;
        }

        @Override
        public Builder<T> post() {
            super.post();
            return this;
        }

        @Override
        public Builder<T> put() {
            super.put();
            return this;
        }

        @Override
        public Builder<T> delete() {
            super.delete();
            return this;
        }

        @Override
        public Builder<T> replace(Parameters parameters) {
            super.replace(parameters);
            return this;
        }

        @Override
        public Builder<T> add(Parameters parameters) {
            super.add(parameters);
            return this;
        }

        @Override
        public Builder<T> add(String name, String value) {
            super.add(name, value);
            return this;
        }

        @Override
        public Builder<T> addIfNot(String name, String value) {
            super.addIfNot(name, value);
            return this;
        }

        @Override
        public Builder<T> addAll(List<String> names, List<String> values) {
            super.addAll(names, values);
            return this;
        }

        @Override
        public Builder<T> set(String name, String value) {
            super.set(name, value);
            return this;
        }

        @Override
        public Builder<T> replaceIfExists(String name, String value) {
            super.replaceIfExists(name, value);
            return this;
        }

        @Override
        public Builder<T> remove(String name) {
            super.remove(name);
            return this;
        }

        @Override
        public Builder<T> clear() {
            super.clear();
            return this;
        }

        @Override
        public Builder<T> addFile(String name, String contentType, File file) {
            super.addFile(name, contentType, file);
            return this;
        }

        @Override
        public Builder<T> addFile(String name, String contentType, File file, UploadListener listener) {
            super.addFile(name, contentType, file, MUploadListener.wrap(listener));
            return this;
        }

        @Override
        public Builder<T> clearFile() {
            super.clearFile();
            return this;
        }

        @Override
        public Builder<T> replaceHeaders(Headers headers) {
            super.replaceHeaders(headers);
            return this;
        }

        @Override
        public Builder<T> addHeaders(Headers headers) {
            super.addHeaders(headers);
            return this;
        }

        @Override
        public Builder<T> addHeader(String name, String value) {
            super.addHeader(name, value);
            return this;
        }

        @Override
        public Builder<T> addHeaderIfNot(String name, String value) {
            super.addHeaderIfNot(name, value);
            return this;
        }

        @Override
        public Builder<T> addHeaders(List<String> names, List<String> values) {
            super.addHeaders(names, values);
            return this;
        }

        @Override
        public Builder<T> setHeader(String name, String value) {
            super.setHeader(name, value);
            return this;
        }

        @Override
        public Builder<T> replaceHeaderIfExists(String name, String value) {
            super.replaceHeaderIfExists(name, value);
            return this;
        }

        @Override
        public Builder<T> removeHeader(String name) {
            super.removeHeader(name);
            return this;
        }

        @Override
        public Builder<T> clearHeaders() {
            super.clearHeaders();
            return this;
        }

        @Override
        public Builder<T> downloadListener(DownloadListener listener) {
            super.downloadListener(MDownloadListener.wrap(listener));
            return this;
        }

        @Override
        public Builder<T> clearDownloadListener() {
            super.clearDownloadListener();
            return this;
        }

        @Override
        public MRequest<T> build() {
            return new MRequest<>(this);
        }
    }
}
