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

package cc.colorcat.netbird.cache;

import cc.colorcat.netbird.Headers;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public final class CacheControl {
    public static final String HEADER_NAME_MAX_AGE = "NetBird-Max-Age";
    public static final long MAX_AGE_NO_CACHE = 0L;
    public static final long MAX_AGE_FOREVER = -1L;

    public static final String HEADER_NAME_CACHE_DATE = "NetBird-Cache-Date";
    public static final long TIME_INVALIDATE = -1L;

    public static long parseMaxAge(Headers headers) {
        String maxAgeHeader = headers.value(HEADER_NAME_MAX_AGE);
        long maxAge = MAX_AGE_NO_CACHE;
        if (maxAgeHeader != null && !maxAgeHeader.isEmpty()) {
            maxAge = Long.parseLong(maxAgeHeader);
        }
        return maxAge;
    }

    public static long parseSaveDate(Headers headers) {
        String saveDateHeader = headers.value(HEADER_NAME_CACHE_DATE);
        long saveDate = TIME_INVALIDATE;
        if (saveDateHeader != null && !saveDateHeader.isEmpty()) {
            saveDate = Long.parseLong(saveDateHeader);
        }
        return saveDate;
    }

    private CacheControl() {
        throw new AssertionError("no instance");
    }
}
