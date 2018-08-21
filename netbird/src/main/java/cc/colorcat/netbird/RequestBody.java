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
import java.io.OutputStream;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public abstract class RequestBody {

    public abstract String contentType();

    /**
     * @return The number of bytes that will be written to {@code output} in a call to {@link #writeTo}, or -1L if unknown.
     */
    public long contentLength() throws IOException {
        return -1L;
    }

    public abstract void writeTo(OutputStream output) throws IOException;
}
