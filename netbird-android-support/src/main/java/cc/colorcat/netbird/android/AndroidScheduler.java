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

import android.os.Handler;
import android.os.Looper;

import cc.colorcat.netbird.Scheduler;

/**
 * Created by cxx on 2018/1/28.
 * xx.ch@outlook.com
 */
public final class AndroidScheduler implements Scheduler {
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public boolean isTargetThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    @Override
    public void onTargetThread(Runnable runnable) {
        handler.post(runnable);
    }
}
