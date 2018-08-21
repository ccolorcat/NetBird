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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
final class FormBody extends RequestBody {
    static FormBody create(Parameters parameters, boolean needEncode) {
        if (!needEncode) return new FormBody(parameters);
        final int size = parameters.size();
        MutableParameters encoded = MutableParameters.create(size);
        for (int i = 0; i < size; ++i) {
            String encodedName = Utils.smartEncode(parameters.name(i));
            String encodedValue = Utils.smartEncode(parameters.value(i));
            encoded.add(encodedName, encodedValue);
        }
        return new FormBody(encoded.toParameters());
    }

    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

    final Parameters parameters;
    private long contentLength = -1L;

    private FormBody(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public String contentType() {
        return FormBody.CONTENT_TYPE;
    }

    @Override
    public long contentLength() throws IOException {
        if (contentLength == -1L) {
            long temp = writeOrCountBytes(null, true);
            if (temp > 0L) {
                contentLength = temp;
            }
        }
        return contentLength;
    }

    @Override
    public void writeTo(OutputStream output) throws IOException {
        writeOrCountBytes(output, false);
    }

    private long writeOrCountBytes(OutputStream output, boolean countBytes) throws IOException {
        long byteCount = 0L;
        OutputStream os = countBytes ? new ByteArrayOutputStream() : output;
        ByteOutputStream bos = new ByteOutputStream(os);
        for (int i = 0, size = parameters.size(); i < size; ++i) {
            if (i > 0) bos.writeByte('&');
            bos.writeUtf8(parameters.name(i));
            bos.writeByte('=');
            bos.writeUtf8(parameters.value(i));
        }
        bos.flush();
        if (countBytes) {
            byteCount = bos.size();
            bos.close();
        }
        return byteCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FormBody formBody = (FormBody) o;

        return parameters.equals(formBody.parameters);
    }

    @Override
    public int hashCode() {
        return parameters.hashCode();
    }

    @Override
    public String toString() {
        return "FormBody{" +
                "parameters=" + parameters +
                '}';
    }
}
