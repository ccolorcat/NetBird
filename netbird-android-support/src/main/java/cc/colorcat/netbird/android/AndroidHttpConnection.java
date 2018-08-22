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


import android.net.http.HttpResponseCache;

import java.io.File;
import java.io.IOException;

import cc.colorcat.netbird.Connection;
import cc.colorcat.netbird.HttpConnection;

/**
 * Created by cxx on 2018/1/28.
 * xx.ch@outlook.com
 */
final class AndroidHttpConnection extends HttpConnection {

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Connection clone() {
        return new AndroidHttpConnection();
    }

    @Override
    protected void enableCache(File cachePath, long cacheSize) {
        if (cachePath != null && cacheSize > 0L) {
            if (!HttpConnection.cacheEnabled) {
                try {
                    HttpResponseCache cache = HttpResponseCache.getInstalled();
                    if (cache == null) {
                        if (cachePath.exists() || cachePath.mkdirs()) {
                            cache = HttpResponseCache.install(cachePath, cacheSize);
                        }
                    }
                    HttpConnection.cacheEnabled = (cache != null);
                } catch (IOException e) {
                    HttpConnection.cacheEnabled = false;
                }
            }
        } else if (HttpConnection.cacheEnabled) {
            HttpConnection.cacheEnabled = false;
            try {
                HttpResponseCache cache = HttpResponseCache.getInstalled();
                if (cache != null) {
                    cache.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
}
