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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public final class FileParser implements Parser<File> {
    public static FileParser create(File savePath) {
        final File parent = savePath.getParentFile();
        if (parent.exists() || parent.mkdirs()) {
            return new FileParser(savePath);
        }
        throw new RuntimeException("Can't create directory, " + savePath.getAbsolutePath());
    }

    public static FileParser create(String savePath) {
        return create(new File(savePath));
    }

    private final File savePath;

    private FileParser(File savePath) {
        this.savePath = savePath;
    }

    @Override
    public NetworkData<? extends File> parse(Response response) throws IOException {
        OutputStream output = null;
        try {
            output = Utils.buffered(new FileOutputStream(savePath));
            Utils.justDump(response.responseBody.stream(), output);
            return NetworkData.newSuccess(savePath);
        } finally {
            Utils.close(output);
        }
    }
}
