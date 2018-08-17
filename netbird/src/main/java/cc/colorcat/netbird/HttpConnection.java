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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public class HttpConnection implements Connection {
    protected static boolean cacheEnabled = false;
    private HttpURLConnection conn;
    private InputStream input;

    protected HttpConnection() {
    }

    @Override
    public final void connect(NetBird netBird, Request request) throws IOException {
        enableCache(netBird.cachePath, netBird.cacheSize);
        final URL url = new URL(request.url);
        final Proxy proxy = netBird.proxy;
        conn = (HttpURLConnection) (proxy == null ? url.openConnection() : url.openConnection(proxy));
        conn.setConnectTimeout(netBird.connectTimeOut);
        conn.setReadTimeout(netBird.readTimeOut);
        conn.setDoInput(true);
        conn.setRequestMethod(request.method.name());
        conn.setDoOutput(request.method.needBody());
        conn.setUseCaches(cacheEnabled);
        if (conn instanceof HttpsURLConnection) {
            final HttpsURLConnection cast = (HttpsURLConnection) conn;
            final SSLSocketFactory sslSocketFactory = netBird.sslSocketFactory;
            if (sslSocketFactory != null) cast.setSSLSocketFactory(sslSocketFactory);
            final HostnameVerifier hostnameVerifier = netBird.hostnameVerifier;
            if (hostnameVerifier != null) cast.setHostnameVerifier(hostnameVerifier);
        }
    }

    @Override
    public final void writeHeaders(Headers headers) throws IOException {
        for (int i = 0, size = headers.size(); i < size; ++i) {
            conn.addRequestProperty(headers.name(i), headers.value(i));
        }
    }

    @Override
    public final void writeRequestBody(RequestBody requestBody) throws IOException {
        final long contentLength = requestBody.contentLength();
        if (contentLength > 0L) {
            OutputStream output = null;
            try {
                output = conn.getOutputStream();
                requestBody.writeTo(output);
                output.flush();
            } finally {
                Utils.close(output);
            }
        }
    }

    @Override
    public final int responseCode() throws IOException {
        return conn.getResponseCode();
    }

    @Override
    public final String responseMsg() throws IOException {
        return conn.getResponseMessage();
    }

    @Override
    public final Headers responseHeaders() throws IOException {
        return Headers.ofWithIgnoreNull(conn.getHeaderFields());
    }

    @Override
    public final ResponseBody responseBody(Headers headers) throws IOException {
        if (input == null) {
            input = conn.getInputStream();
        }
        return input != null ? ResponseBody.create(input, headers) : null;
    }

    @Override
    public final void cancel() {
        if (conn != null) {
            conn.disconnect();
        }
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Connection clone() {
        return new HttpConnection();
    }

    @Override
    public final void close() throws IOException {
        Utils.close(input);
        if (conn != null) {
            conn.disconnect();
        }
    }

    protected void enableCache(File cachePath, long cacheSize) {
        HttpConnection.cacheEnabled = (cachePath != null && cacheSize > 0L);
    }
}
