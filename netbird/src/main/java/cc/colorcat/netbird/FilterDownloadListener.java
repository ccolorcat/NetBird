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
final class FilterDownloadListener implements DownloadListener {
    static DownloadListener of(DownloadListener listener) {
        return listener != null ? new FilterDownloadListener(listener) : null;
    }

    private final DownloadListener listener;

    private FilterDownloadListener(DownloadListener listener) {
        this.listener = listener;
    }

    @Override
    public void onChanged(final long finished, final long total, final int percent) {
        if (Utils.isTargetThread()) {
            listener.onChanged(finished, total, percent);
        } else {
            Utils.onTargetThread(new Runnable() {
                @Override
                public void run() {
                    listener.onChanged(finished, total, percent);
                }
            });
        }
    }
}