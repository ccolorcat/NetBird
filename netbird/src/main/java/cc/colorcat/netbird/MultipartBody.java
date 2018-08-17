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
import java.util.List;
import java.util.Objects;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
final class MultipartBody extends RequestBody {
    static MultipartBody create(FormBody formBody, List<FileBody> fileBodies, String boundary) {
        return new MultipartBody(formBody, fileBodies, boundary);
    }

    private static final String MIX = "multipart/form-data; boundary=";
    private static final byte[] CRLF = {'\r', '\n'};
    private static final byte[] DASH_DASH = {'-', '-'};

    private final FormBody formBody;
    private final List<FileBody> fileBodies;
    private final String boundary;
    private long contentLength = -1L;

    private MultipartBody(FormBody formBody, List<FileBody> fileBodies, String boundary) {
        this.formBody = formBody;
        this.fileBodies = fileBodies;
        this.boundary = boundary;
    }

    @Override
    public String contentType() {
        return MultipartBody.MIX + boundary;
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

        Parameters parameters = formBody.parameters;
        for (int i = 0, size = parameters.size(); i < size; ++i) {
            String name = parameters.name(i);
            String value = parameters.value(i);

            bos.write(DASH_DASH);
            bos.writeUtf8(boundary);
            bos.write(CRLF);

            bos.writeUtf8("Content-Disposition: form-data; name=\"" + name + "\"");
            bos.write(CRLF);

            bos.writeUtf8("Content-Type: " + formBody.contentType());
            bos.write(CRLF);

            bos.write(CRLF);
            bos.writeUtf8(value);
            bos.write(CRLF);
        }

        for (int i = 0, size = fileBodies.size(); i < size; ++i) {
            FileBody body = fileBodies.get(i);
            long contentLength = body.contentLength();
            if (contentLength != -1) {
                bos.write(DASH_DASH);
                bos.writeUtf8(boundary);
                bos.write(CRLF);

                bos.writeUtf8("Content-Disposition: form-data; name=\"" + body.name + "\"; filename=\"" + body.file.getName() + "\"");
                bos.write(CRLF);

                bos.writeUtf8("Content-Type: " + body.contentType());
                bos.write(CRLF);

                bos.writeUtf8("Content-Transfer-Encoding: BINARY");
                bos.write(CRLF);

                bos.write(CRLF);
                if (countBytes) {
                    byteCount += contentLength;
                } else {
                    body.writeTo(bos);
                }

                bos.write(CRLF);
            } else if (countBytes) {
                bos.close();
                return -1L;
            }
        }

        bos.write(CRLF);
        bos.write(DASH_DASH);
        bos.writeUtf8(boundary);
        bos.write(DASH_DASH);
        bos.write(CRLF);
        bos.flush();

        if (countBytes) {
            byteCount += bos.size();
            bos.close();
        }

        return byteCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultipartBody that = (MultipartBody) o;
        return Objects.equals(formBody, that.formBody) &&
                Objects.equals(fileBodies, that.fileBodies) &&
                Objects.equals(boundary, that.boundary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formBody, fileBodies, boundary);
    }

    @Override
    public String toString() {
        return "MultipartBody{" +
                "formBody=" + formBody +
                ", fileBodies=" + fileBodies +
                ", boundary='" + boundary + '\'' +
                '}';
    }
}
