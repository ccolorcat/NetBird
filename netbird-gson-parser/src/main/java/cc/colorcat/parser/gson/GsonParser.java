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

package cc.colorcat.parser.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.$Gson$Types;

import java.io.IOException;
import java.io.Reader;

import cc.colorcat.netbird.JsonParser;
import cc.colorcat.netbird.NetworkData;
import cc.colorcat.netbird.Response;
import cc.colorcat.netbird.StateIOException;

/**
 * Author: cxx
 * Date: 2018-08-22
 * GitHub: https://github.com/ccolorcat
 */
public abstract class GsonParser<T> extends JsonParser<T> {
    private static Gson gson = new GsonBuilder().create();

    public static void setGson(Gson gson) {
        if (gson == null) throw new IllegalArgumentException("gson == null");
        GsonParser.gson = gson;
    }

    @Override
    public NetworkData<? extends T> parse(Response response) throws IOException {
        try {
            Reader reader = response.responseBody().reader(charsetIfAbsent());
            T data = GsonParser.gson.fromJson(reader, $Gson$Types.canonicalize(generateType()));
            if (data != null) {
                return NetworkData.newSuccess(data);
            }
            return NetworkData.newFailure(response.responseCode(), response.responseMsg());
        } catch (Exception e) {
            throw new StateIOException(response.responseCode(), response.responseMsg(), e);
        }
    }
}
