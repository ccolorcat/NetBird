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

import java.util.ArrayList;
import java.util.List;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public final class MutableHeaders extends Headers implements PairWriter {
    public static MutableHeaders create(int initCapacity) {
        List<String> names = new ArrayList<>(initCapacity);
        List<String> values = new ArrayList<>(initCapacity);
        return new MutableHeaders(new MutablePair(names, values, String.CASE_INSENSITIVE_ORDER));
    }


    MutableHeaders(MutablePair pair) {
        super(pair);
    }

    public void addLine(String line) {
        int index = line.indexOf(":");
        if (index == -1) throw new IllegalArgumentException("Unexpected header: " + line);
        add(line.substring(0, index).trim(), line.substring(index + 1).trim());
    }

    @Override
    public void add(String name, String value) {
        checkNameAndValue(name, value);
        cast().add(name, value);
    }

    @Override
    public void addIfNot(String name, String value) {
        checkNameAndValue(name, value);
        cast().add(name, value);
    }

    @Override
    public void addAll(List<String> names, List<String> values) {
        for (int i = 0, size = names.size(); i < size; ++i) {
            checkNameAndValue(names.get(i), values.get(i));
        }
        cast().addAll(names, values);
    }

    @Override
    public void set(String name, String value) {
        checkNameAndValue(name, value);
        cast().set(name, value);
    }

    @Override
    public void replaceIfExists(String name, String value) {
        checkNameAndValue(name, value);
        cast().replaceIfExists(name, value);
    }

    @Override
    public void removeAll(String name) {
        cast().removeAll(name);
    }

    @Override
    public void clear() {
        cast().clear();
    }

    public Headers toHeaders() {
        return new Headers(cast().toPair());
    }

    private MutablePair cast() {
        return (MutablePair) delegate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MutableHeaders that = (MutableHeaders) o;

        return delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return 19 * delegate.hashCode();
    }


    private static void checkNameAndValue(String name, String value) {
        if (name == null) throw new NullPointerException("name == null");
        if (name.isEmpty()) throw new IllegalArgumentException("name is empty");
        for (int i = 0, length = name.length(); i < length; i++) {
            char c = name.charAt(i);
            if (c <= '\u0020' || c >= '\u007f') {
                throw new IllegalArgumentException("Unexpected char " + c + " at " + i + " in header name: " + name);
            }
        }
        if (value == null) throw new NullPointerException("value == null");
        for (int i = 0, length = value.length(); i < length; i++) {
            char c = value.charAt(i);
            if ((c <= '\u001f' && c != '\t') || c >= '\u007f') {
                throw new IllegalArgumentException("Unexpected char " + c + " at " + i + " in header value: " + value);
            }
        }
    }
}
