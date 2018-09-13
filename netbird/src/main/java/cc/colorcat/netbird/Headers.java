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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public class Headers implements PairReader {
    public static Headers ofWithIgnoreNull(List<String> names, List<String> values) {
        final int ns = names.size(), vs = values.size();
        if (ns != vs) throw new IllegalArgumentException("names.size(" + ns + ") != values.size(" + vs + ')');
        if (ns == 0) return Headers.EMPTY;
        List<String> newNames = new ArrayList<>(ns);
        List<String> newValues = new ArrayList<>(ns);
        for (int i = 0; i < ns; ++i) {
            String name = names.get(i);
            String value = values.get(i);
            if (name == null || value == null) continue;
            newNames.add(name);
            newValues.add(value);
        }
        return new Headers(new Pair(newNames, newValues, Pair.CASE_INSENSITIVE_ORDER));
    }

    public static Headers ofWithIgnoreNull(Map<String, List<String>> namesAndValues) {
        if (namesAndValues.isEmpty()) return Headers.EMPTY;
        Entry<List<String>, List<String>> entries = Utils.unzipWithIgnoreNull(namesAndValues);
        return new Headers(new Pair(entries.first, entries.second, Pair.CASE_INSENSITIVE_ORDER));
    }

    static final Headers EMPTY = new Headers(Pair.EMPTY_CASE_INSENSITIVE);
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_LENGTH = "Content-Length";


    final Pair delegate;

    Headers(Pair pair) {
        delegate = pair;
    }

    public final String contentType() {
        return delegate.value(Headers.CONTENT_TYPE);
    }

    public final long contentLength() {
        return Utils.quiteToLong(delegate.value(Headers.CONTENT_LENGTH), -1L);
    }

    public final Charset charset() {
        return Utils.parseCharset(contentType(), null);
    }

    public final Charset charset(Charset defaultValue) {
        return Utils.parseCharset(contentType(), defaultValue);
    }

    @Override
    public final int size() {
        return delegate.size();
    }

    @Override
    public final boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public final boolean contains(String name) {
        return delegate.contains(name);
    }

    @Override
    public final List<String> names() {
        return delegate.names();
    }

    @Override
    public final List<String> values() {
        return delegate.values();
    }

    @Override
    public final String name(int index) {
        return delegate.name(index);
    }

    @Override
    public final String value(int index) {
        return delegate.value(index);
    }

    @Override
    public final String value(String name) {
        return delegate.value(name);
    }

    @Override
    public final String value(String name, String defaultValue) {
        return delegate.value(name, defaultValue);
    }

    @Override
    public final List<String> values(String name) {
        return delegate.values(name);
    }

    @Override
    public final Set<String> nameSet() {
        return delegate.nameSet();
    }

    @Override
    public final Map<String, List<String>> toMultimap() {
        return delegate.toMultimap();
    }

    @Override
    public final Iterator<NameAndValue> iterator() {
        return delegate.iterator();
    }

    public final String contentToString(String nameValueSeparator, String lineSeparator) {
        return delegate.contentToString(nameValueSeparator, lineSeparator);
    }

    public final String toMultiLine() {
        return delegate.contentToString(":", "\n");
    }

    public final MutableHeaders toMutableHeaders() {
        return new MutableHeaders(delegate.toMutablePair());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Headers that = (Headers) o;

        return delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return 17 * delegate.hashCode();
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + '{' + delegate.contentToString("=", ", ") + '}';
    }
}
