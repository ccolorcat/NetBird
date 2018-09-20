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

package cc.colorcat.netbird.android;

import android.util.Log;

import cc.colorcat.netbird.Level;
import cc.colorcat.netbird.Logger;

/**
 * Created by cxx on 2018/1/28.
 * xx.ch@outlook.com
 */
final class AndroidLogger implements Logger {
    private static final int MAX_LENGTH = 1024 * 4;

    @Override
    public void log(String tag, String msg, Level level) {
        switch (level) {
            case VERBOSE:
                println(tag, msg, Log.VERBOSE);
                break;
            case DEBUG:
                println(tag, msg, Log.DEBUG);
                break;
            case INFO:
                println(tag, msg, Log.INFO);
                break;
            case WARN:
                println(tag, msg, Log.WARN);
                break;
            case ERROR:
                println(tag, msg, Log.ERROR);
                break;
            default:
                break;
        }
    }

    private static void println(String tag, String msg, int priority) {
        for (int start = 0, end = start + MAX_LENGTH, size = msg.length(); start < size; start = end, end = start + MAX_LENGTH) {
            if (end >= size) {
                Log.println(priority, tag, msg.substring(start));
            } else {
                Log.println(priority, tag, msg.substring(start, end));
            }
        }
    }
}
