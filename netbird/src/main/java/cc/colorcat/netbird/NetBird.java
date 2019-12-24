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
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public final class NetBird implements Call.Factory {
    private final Platform platform;
    final String baseUrl;
    final List<Interceptor> headInterceptors;
    final List<Interceptor> tailInterceptors;
    final ExecutorService executor;
    final Dispatcher dispatcher;
    final Connection connection;
    final HeaderManager headerManager;
    final Proxy proxy;
    final SSLSocketFactory sslSocketFactory;
    final HostnameVerifier hostnameVerifier;
    final long cacheSize;
    final File cachePath;
    final int maxRunning;
    final int readTimeOut;
    final int connectTimeOut;
    final boolean gzipEnabled;

    private NetBird(Builder builder) {
        this.platform = builder.platform;
        this.baseUrl = builder.baseUrl;
        this.headInterceptors = Utils.immutableList(builder.headInterceptors);
        this.tailInterceptors = Utils.immutableList(builder.tailInterceptors);
        this.executor = builder.executor;
        this.dispatcher = builder.dispatcher;
        this.connection = builder.connection;
        this.headerManager = builder.headerManager;
        this.proxy = builder.proxy;
        this.sslSocketFactory = builder.sslSocketFactory;
        this.hostnameVerifier = builder.hostnameVerifier;
        this.cacheSize = builder.cacheSize;
        this.cachePath = builder.cachePath;
        this.maxRunning = builder.maxRunning;
        this.readTimeOut = builder.readTimeOut;
        this.connectTimeOut = builder.connectTimeOut;
        this.gzipEnabled = builder.gzipEnabled;
        Platform.instance = this.platform;
        dispatcher.setExecutor(this.executor);
        dispatcher.setMaxRunning(this.maxRunning);
        Log.threshold = builder.logLevel;
    }

    @Override
    public Call newCall(Request request) {
        return new RealCall(this, request);
    }

    public void cancelWaiting(Object tag) {
        if (tag != null) {
            dispatcher.cancelWaiting(tag);
        }
    }

    public void cancelAll(Object tag) {
        if (tag != null) {
            dispatcher.cancelAll(tag);
        }
    }

    public void cancelAll() {
        dispatcher.cancelAll();
    }

    public String baseUrl() {
        return baseUrl;
    }

    public List<Interceptor> headInterceptors() {
        return headInterceptors;
    }

    public List<Interceptor> tailInterceptors() {
        return tailInterceptors;
    }

    public Connection connection() {
        return connection.clone();
    }

    public HeaderManager headerManager() {
        return headerManager;
    }

    public Proxy proxy() {
        return proxy;
    }

    public SSLSocketFactory sslSocketFactory() {
        return sslSocketFactory;
    }

    public HostnameVerifier hostnameVerifier() {
        return hostnameVerifier;
    }

    public long cacheSize() {
        return cacheSize;
    }

    public File cachePath() {
        return cachePath;
    }

    public int maxRunning() {
        return maxRunning;
    }

    public int readTimeOut() {
        return readTimeOut;
    }

    public int connectTimeOut() {
        return connectTimeOut;
    }

    public boolean gzipEnabled() {
        return gzipEnabled;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }


    public static final class Builder {
        private Platform platform;
        private String baseUrl;
        private List<Interceptor> headInterceptors;
        private List<Interceptor> tailInterceptors;
        private ExecutorService executor;
        private Dispatcher dispatcher;
        private Connection connection;
        private HeaderManager headerManager;
        private Proxy proxy;
        private SSLSocketFactory sslSocketFactory;
        private HostnameVerifier hostnameVerifier;
        private long cacheSize;
        private File cachePath;
        private int maxRunning;
        private int readTimeOut;
        private int connectTimeOut;
        private boolean gzipEnabled;
        private Level logLevel;

        public Builder(String baseUrl) {
            this.baseUrl = Utils.checkedUrl(baseUrl);
            this.platform = Platform.get();
            this.headInterceptors = new ArrayList<>(2);
            this.tailInterceptors = new ArrayList<>(2);
            this.executor = null;
            this.dispatcher = new Dispatcher();
            this.connection = this.platform.connection();
            this.headerManager = HeaderManager.EMPTY;
            this.proxy = null;
            this.sslSocketFactory = null;
            this.hostnameVerifier = null;
            this.cacheSize = -1L;
            this.cachePath = null;
            this.maxRunning = 6;
            this.readTimeOut = 10000;
            this.connectTimeOut = 10000;
            this.gzipEnabled = false;
            this.logLevel = Level.NOTHING;
        }

        private Builder(NetBird netBird) {
            this.platform = netBird.platform;
            this.baseUrl = netBird.baseUrl;
            this.headInterceptors = new ArrayList<>(netBird.headInterceptors);
            this.tailInterceptors = new ArrayList<>(netBird.tailInterceptors);
            this.executor = netBird.executor;
            this.dispatcher = netBird.dispatcher;
            this.connection = netBird.connection;
            this.headerManager = netBird.headerManager;
            this.proxy = netBird.proxy;
            this.sslSocketFactory = netBird.sslSocketFactory;
            this.hostnameVerifier = netBird.hostnameVerifier;
            this.cacheSize = netBird.cacheSize;
            this.cachePath = netBird.cachePath;
            this.maxRunning = netBird.maxRunning;
            this.readTimeOut = netBird.readTimeOut;
            this.connectTimeOut = netBird.connectTimeOut;
            this.gzipEnabled = netBird.gzipEnabled;
            this.logLevel = Log.threshold;
        }

        public Builder platform(Platform platform) {
            this.platform = platform;
            this.connection = platform.connection();
            return this;
        }

        public Builder addHeadInterceptor(Interceptor interceptor) {
            if (interceptor == null) throw new IllegalArgumentException("interceptor == null");
            this.headInterceptors.add(interceptor);
            return this;
        }

        public Builder removeHeadInterceptor(Interceptor interceptor) {
            this.headInterceptors.remove(interceptor);
            return this;
        }

        public Builder addTailInterceptor(Interceptor interceptor) {
            if (interceptor == null) throw new IllegalArgumentException("interceptor == null");
            this.tailInterceptors.add(interceptor);
            return this;
        }

        public Builder removeTailInterceptor(Interceptor interceptor) {
            this.tailInterceptors.remove(interceptor);
            return this;
        }

        public Builder executor(ExecutorService executor) {
            if (executor == null) throw new IllegalArgumentException("executor == null");
            this.executor = executor;
            return this;
        }

        public Builder connection(Connection connection) {
            if (connection == null) throw new IllegalArgumentException("connection == null");
            this.connection = connection;
            return this;
        }

        public Builder headerManager(HeaderManager headerManager) {
            if (headerManager == null) throw new IllegalArgumentException("headerManager == null");
            this.headerManager = headerManager;
            return this;
        }

        public Builder proxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
            this.sslSocketFactory = sslSocketFactory;
            return this;
        }

        public Builder hostnameVerifier(HostnameVerifier hostnameVerifier) {
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        public Builder cache(File cachePath, long cacheSize) {
            if (!cachePath.exists()) {
                throw new IllegalArgumentException(cachePath.getAbsolutePath() + " is not exists");
            }
            if (cacheSize <= 0L) {
                throw new IllegalArgumentException("cacheSize(" + cacheSize + ") <= 0L");
            }
            this.cachePath = cachePath;
            this.cacheSize = cacheSize;
            return this;
        }

        public Builder maxRunning(int maxRunning) {
            if (maxRunning < 1) {
                throw new IllegalArgumentException("maxRunning(" + maxRunning + ") < 1");
            }
            this.maxRunning = maxRunning;
            return this;
        }

        public Builder readTimeOut(int milliseconds) {
            if (milliseconds <= 0) {
                throw new IllegalArgumentException("readTimeOut(" + milliseconds + ") < 0");
            }
            this.readTimeOut = milliseconds;
            return this;
        }

        public Builder connectTimeOut(int milliseconds) {
            if (milliseconds <= 0) {
                throw new IllegalArgumentException("connectTimeOut(" + milliseconds + ") < 0");
            }
            this.connectTimeOut = milliseconds;
            return this;
        }

        public Builder enableGzip(boolean enabled) {
            this.gzipEnabled = enabled;
            return this;
        }

        public Builder logLevel(Level level) {
            if (level == null) throw new IllegalArgumentException("level == null");
            this.logLevel = level;
            return this;
        }

        public NetBird build() {
            if (executor == null) executor = Utils.defaultService(maxRunning);
            return new NetBird(this);
        }
    }
}
