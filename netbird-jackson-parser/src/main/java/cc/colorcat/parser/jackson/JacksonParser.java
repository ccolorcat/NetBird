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

package cc.colorcat.parser.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.io.Reader;

import cc.colorcat.netbird.JsonParser;
import cc.colorcat.netbird.NetworkData;
import cc.colorcat.netbird.Response;
import cc.colorcat.netbird.StateIOException;

/**
 * Author: cxx
 * Date: 2018-08-23
 * GitHub: https://github.com/ccolorcat
 */
public abstract class JacksonParser<T> extends JsonParser<T> {
    private static ObjectMapper mapper = new ObjectMapper();

    public static void setObjectMapper(ObjectMapper mapper) {
        if (mapper == null) throw new IllegalArgumentException("mapper == null");
        JacksonParser.mapper = mapper;
    }

    @Override
    public NetworkData<? extends T> parse(Response response) throws IOException {
        try {
            Reader reader = response.responseBody().reader(charsetIfAbsent());
            T data = mapper.readValue(reader, TypeFactory.defaultInstance().constructType(generateType()));
            if (data != null) {
                return NetworkData.newSuccess(data);
            }
            return NetworkData.newFailure(response.responseCode(), response.responseMsg());
        } catch (Exception e) {
            throw new StateIOException(response.responseCode(), response.responseMsg(), e);
        }
    }
}
