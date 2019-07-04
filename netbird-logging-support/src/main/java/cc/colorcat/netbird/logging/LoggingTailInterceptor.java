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

package cc.colorcat.netbird.logging;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.colorcat.netbird.FileBody;
import cc.colorcat.netbird.Headers;
import cc.colorcat.netbird.Interceptor;
import cc.colorcat.netbird.Level;
import cc.colorcat.netbird.NameAndValue;
import cc.colorcat.netbird.NetBird;
import cc.colorcat.netbird.PairReader;
import cc.colorcat.netbird.Platform;
import cc.colorcat.netbird.Request;
import cc.colorcat.netbird.Response;
import cc.colorcat.netbird.ResponseBody;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public class LoggingTailInterceptor implements Interceptor {
    private static final String TAG = NetBird.class.getSimpleName();
    private static final String LINE = buildString(94, '-');
    private static final String HALF_LINE = buildString(38, '-');
    private final Charset charsetIfAbsent;
    private final boolean deUnicode;

    public LoggingTailInterceptor() {
        this(Charset.forName("UTF-8"), false);
    }

    public LoggingTailInterceptor(boolean deUnicode) {
        this(Charset.forName("UTF-8"), deUnicode);
    }

    public LoggingTailInterceptor(Charset charsetIfAbsent, boolean deUnicode) {
        this.charsetIfAbsent = charsetIfAbsent;
        this.deUnicode = deUnicode;
    }

    @Override
    public final Response intercept(Chain chain) throws IOException {
        final Request request = chain.request();
        long start = System.currentTimeMillis();
        Response response = chain.proceed(request);
        long elapse = System.currentTimeMillis() - start;

        final StringBuilder builder = new StringBuilder();
        builder.append(" \n").append(HALF_LINE)
                .append(' ').append(request.method().name())
                .append(" (").append(elapse).append(" millis) ")
                .append(HALF_LINE).append('>')
                .append("\nrequest url --> ").append(request.url());
        appendPair(builder, "request header --> ", request.headers());

        if (request.method().needBody()) {
            appendPair(builder, "request parameter --> ", request.parameters());
            appendFile(builder, "request file --> ", request.fileBodies());
        }

        builder.append("\n\nresponse --> ").append(response.responseCode()).append("--").append(response.responseMsg());
        appendPair(builder, "response header --> ", response.headers());
        ResponseBody body = response.responseBody();
        if (body != null) {
            final String contentType = body.contentType();
            if (contentType != null && contentFilter(contentType)) {
                final byte[] bytes = body.bytes();
                Charset charset = body.charset();
                if (charset == null) charset = charsetIfAbsent;
                final String content = new String(bytes, charset);
                builder.append("\nresponse content --> ").append(formatResponse(deUnicode ? decode(content) : content, contentType));
                final ResponseBody newBody = ResponseBody.create(bytes, contentType, charset);
                response = response.newBuilder()
                        .setHeader(Headers.CONTENT_LENGTH, Long.toString(newBody.contentLength()))
                        .responseBody(newBody)
                        .build();
            }
        }
        builder.append('\n').append('<').append(LINE);
        Platform.get().logger().log(TAG, builder.toString(), Level.INFO);
        return response;
    }

    protected boolean contentFilter(String contentType) {
        return contentType.matches(".*(charset|text|html|htm|json|urlencoded)+.*");
    }

    protected String formatResponse(String content, String contentType) {
        return content;
    }

    private static void appendPair(StringBuilder builder, String prefix, PairReader reader) {
        for (NameAndValue nv : reader) {
            builder.append('\n').append(prefix).append(nv.name).append('=').append(nv.value);
        }
    }

    private static void appendFile(StringBuilder builder, String prefix, List<FileBody> bodies) {
        for (FileBody body : bodies) {
            builder.append('\n').append(prefix).append(body.toString());
        }
    }

    private static String buildString(int count, char c) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; ++i) {
            builder.append(c);
        }
        return builder.toString();
    }

    private static String decode(String unicode) {
        StringBuilder builder = new StringBuilder(unicode.length());
        Matcher matcher = Pattern.compile("\\\\u[0-9a-fA-F]{4}").matcher(unicode);
        int last = 0;
        for (int start, end = 0; matcher.find(end); last = end) {
            start = matcher.start();
            end = matcher.end();
            builder.append(unicode.substring(last, start))
                    .append((char) Integer.parseInt(unicode.substring(start + 2, end), 16));
        }
        return builder.append(unicode.substring(last)).toString();
    }
}
