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
import java.nio.charset.Charset;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public final class StringParser implements Parser<String> {
    private static volatile StringParser utf8;
    private static volatile StringParser defaultStringParser;

    public static StringParser getUtf8() {
        if (utf8 == null) {
            synchronized (StringParser.class) {
                if (utf8 == null) {
                    utf8 = new StringParser(Utils.UTF8);
                }
            }
        }
        return utf8;
    }

    public static StringParser getDefault() {
        if (defaultStringParser == null) {
            synchronized (StringParser.class) {
                if (defaultStringParser == null) {
                    defaultStringParser = new StringParser(null);
                }
            }
        }
        return defaultStringParser;
    }

    public static StringParser create(String charsetIfAbsent) {
        if (Utils.isEmpty(charsetIfAbsent)) return getDefault();
        final Charset charset = Charset.forName(charsetIfAbsent);
        if (Utils.UTF8.equals(charset)) return getUtf8();
        return new StringParser(charset);
    }

    public static StringParser create(Charset ifAbsent) {
        if (ifAbsent == null) return getDefault();
        if (Utils.UTF8.equals(ifAbsent)) return getUtf8();
        return new StringParser(ifAbsent);
    }

    private final Charset ifAbsent;

    private StringParser(Charset ifAbsent) {
        this.ifAbsent = ifAbsent;
    }

    @Override
    public NetworkData<? extends String> parse(Response response) throws IOException {
        final ResponseBody body = response.responseBody;
        return ifAbsent != null ? NetworkData.newSuccess(body.string(ifAbsent)) : NetworkData.newSuccess(body.string());
    }
}
