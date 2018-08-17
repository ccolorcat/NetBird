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

import cc.colorcat.netbird.Connection;
import cc.colorcat.netbird.Logger;
import cc.colorcat.netbird.Platform;
import cc.colorcat.netbird.Scheduler;

/**
 * Created by cxx on 2018/1/28.
 * xx.ch@outlook.com
 */
public final class AndroidPlatform extends Platform {
    private final Connection connection = new AndroidHttpConnection();
    private final Scheduler scheduler = new AndroidScheduler();
    private final Logger logger = new AndroidLogger();

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public Scheduler scheduler() {
        return scheduler;
    }

    @Override
    public Logger logger() {
        return logger;
    }
}
