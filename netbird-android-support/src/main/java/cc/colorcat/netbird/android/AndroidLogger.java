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
public class AndroidLogger implements Logger {
    protected static final int MAX_LENGTH = 1024 * 2;

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

    protected void println(String tag, String msg, int priority) {
        final int length = msg.length();
        if (length <= MAX_LENGTH) {
            Log.println(priority, tag, msg);
            return;
        }
        for (int start = 0, end; start < length; start = end) {
            end = friendlyEnd(msg, start, Math.min(start + MAX_LENGTH, length));
            Log.println(priority, tag, msg.substring(start, end));
        }
    }

    protected static int friendlyEnd(String msg, int start, int end) {
        if (msg.length() == end || msg.charAt(end) == '\n') {
            return end;
        }
        for (int last = end - 1; last > start; --last) {
            if (msg.charAt(last) == '\n') {
                return last + 1;
            }
        }
        return end;
    }
}
