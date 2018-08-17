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
final class Log {
    static Level threshold = Level.NOTHING;

    static void v(String tag, String msg) {
        log(tag, msg, Level.VERBOSE);
    }

    static void d(String tag, String msg) {
        log(tag, msg, Level.DEBUG);
    }

    static void i(String tag, String msg) {
        log(tag, msg, Level.INFO);
    }

    static void w(String tag, String msg) {
        log(tag, msg, Level.WARN);
    }

    static void e(String tag, String msg) {
        log(tag, msg, Level.ERROR);
    }

    static void e(Throwable throwable) {
        if (Level.ERROR.priority >= threshold.priority) {
            throwable.printStackTrace();
        }
    }

    private static void log(String tag, String msg, Level level) {
        if (level.priority >= threshold.priority) {
            Platform.get().logger().log(tag, msg, level);
        }
    }

    private Log() {
        throw new AssertionError("no instance");
    }
}
