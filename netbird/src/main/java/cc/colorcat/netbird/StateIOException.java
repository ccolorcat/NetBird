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

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public class StateIOException extends IOException {
    int state;

    public StateIOException(int state, String message) {
        super(message);
        this.state = state;
    }

    public StateIOException(int state, String message, Throwable cause) {
        super(message, cause);
        this.state = state;
    }

    public StateIOException(int state, Throwable cause) {
        super(cause);
        this.state = state;
    }

    public int state() {
        return this.state;
    }

    @Override
    public String toString() {
        return "StateIOException{" +
                "state=" + state +
                ", message=" + getMessage() +
                ", cause=" + getCause() +
                '}';
    }
}
