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

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public final class HttpStatus {
    public static final int CODE_CONNECT_ERROR = -100;
    public static final String MSG_CONNECT_ERROR = "connect error";

    public static final int CODE_DUPLICATE_REQUEST = -101;
    public static final String MSG_DUPLICATE_REQUEST = "duplicate request";

    public static final int CODE_REQUEST_CANCELED = -102;
    public static final String MSG_REQUEST_CANCELED = "request canceled";

    private static volatile StateIOException duplicateRequest;
    private static volatile StateIOException requestCanceled;

    static StateIOException duplicateRequest() {
        if (duplicateRequest == null) {
            synchronized (HttpStatus.class) {
                if (duplicateRequest == null) {
                    duplicateRequest = new StateIOException(CODE_DUPLICATE_REQUEST, MSG_DUPLICATE_REQUEST);
                }
            }
        }
        return duplicateRequest;
    }

    static StateIOException requestCanceled() {
        if (requestCanceled == null) {
            synchronized (HttpStatus.class) {
                if (requestCanceled == null) {
                    requestCanceled = new StateIOException(CODE_REQUEST_CANCELED, MSG_REQUEST_CANCELED);
                }
            }
        }
        return requestCanceled;
    }

    private HttpStatus() {
        throw new AssertionError("no instance");
    }
}
