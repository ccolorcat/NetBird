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

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public final class GenericLogger implements Logger {
    private static final java.util.logging.Logger logger;

    static {
        logger = java.util.logging.Logger.getLogger(NetBird.class.getSimpleName());
        final Formatter formatter = new Formatter() {
            @Override
            public synchronized String format(LogRecord record) {
                return String.format("%-7s %s\n", toLevel(record.getLevel()).name(), record.getMessage());
            }
        };
        logger.setUseParentHandlers(false);
        final ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        final java.util.logging.Level level = java.util.logging.Level.ALL;
        handler.setLevel(level);
        logger.addHandler(handler);
        logger.setLevel(level);
    }

    @Override
    public void log(String tag, String msg, Level level) {
        String log = tag + " --> " + msg;
        switch (level) {
            case VERBOSE:
                logger.log(java.util.logging.Level.FINE, log);
                break;
            case DEBUG:
                logger.log(java.util.logging.Level.CONFIG, log);
                break;
            case INFO:
                logger.log(java.util.logging.Level.INFO, log);
                break;
            case WARN:
                logger.log(java.util.logging.Level.WARNING, log);
                break;
            case ERROR:
                logger.log(java.util.logging.Level.SEVERE, log);
                break;
            default:
                break;
        }
    }

    private static Level toLevel(java.util.logging.Level level) {
        final int value = level.intValue();
        if (value == java.util.logging.Level.FINE.intValue()) {
            return Level.VERBOSE;
        }
        if (value == java.util.logging.Level.CONFIG.intValue()) {
            return Level.DEBUG;
        }
        if (value == java.util.logging.Level.INFO.intValue()) {
            return Level.INFO;
        }
        if (value == java.util.logging.Level.WARNING.intValue()) {
            return Level.WARN;
        }
        if (value == java.util.logging.Level.SEVERE.intValue()) {
            return Level.ERROR;
        }
        throw new IllegalArgumentException("unsupported level = " + level);
    }
}
