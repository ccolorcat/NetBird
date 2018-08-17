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

import java.io.Closeable;
import java.io.IOException;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public interface Connection extends Closeable, Cloneable {

    void connect(NetBird netBird, Request request) throws IOException;

    void writeHeaders(Headers headers) throws IOException;

    void writeRequestBody(RequestBody requestBody) throws IOException;

    int responseCode() throws IOException;

    String responseMsg() throws IOException;

    Headers responseHeaders() throws IOException;

    ResponseBody responseBody(Headers headers) throws IOException;

    void cancel();

    Connection clone();
}
