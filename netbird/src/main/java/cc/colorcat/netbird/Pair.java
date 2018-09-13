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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
class Pair implements PairReader {
    static final Comparator<String> CASE_INSENSITIVE_ORDER = String.CASE_INSENSITIVE_ORDER;
    static final Comparator<String> CASE_SENSITIVE_ORDER = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    };

    static final Pair EMPTY_CASE_INSENSITIVE = new Pair(Collections.<String>emptyList(), Collections.<String>emptyList(), Pair.CASE_INSENSITIVE_ORDER);
    static final Pair EMPTY_CASE_SENSITIVE = new Pair(Collections.<String>emptyList(), Collections.<String>emptyList(), Pair.CASE_SENSITIVE_ORDER);


    final List<String> names;
    final List<String> values;
    final Comparator<String> comparator;

    Pair(List<String> names, List<String> values, Comparator<String> comparator) {
        this.names = names;
        this.values = values;
        this.comparator = comparator;
    }

    @Override
    public final int size() {
        return names.size();
    }

    @Override
    public final boolean isEmpty() {
        return names.isEmpty();
    }

    @Override
    public final boolean contains(String name) {
        for (String e : names) {
            if (equal(name, e)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final List<String> names() {
        return Utils.immutableList(names);
    }

    @Override
    public final List<String> values() {
        return Utils.immutableList(values);
    }

    @Override
    public final String name(int index) {
        return names.get(index);
    }

    @Override
    public final String value(int index) {
        return values.get(index);
    }

    @Override
    public final String value(String name) {
        for (int i = 0, size = names.size(); i < size; ++i) {
            if (equal(name, names.get(i))) {
                return values.get(i);
            }
        }
        return null;
    }

    @Override
    public final String value(String name, String defaultValue) {
        return Utils.nullElse(value(name), defaultValue);
    }

    @Override
    public final List<String> values(String name) {
        List<String> result = null;
        for (int i = 0, size = names.size(); i < size; ++i) {
            if (equal(name, names.get(i))) {
                if (result == null) result = new ArrayList<>(2);
                result.add(values.get(i));
            }
        }
        return result == null ? Collections.<String>emptyList() : Collections.unmodifiableList(result);
    }

    @Override
    public final Set<String> nameSet() {
        if (names.isEmpty()) return Collections.emptySet();
        Set<String> result = new TreeSet<>(comparator);
        result.addAll(names);
        return Collections.unmodifiableSet(result);
    }

    @Override
    public final Map<String, List<String>> toMultimap() {
        if (names.isEmpty()) return Collections.emptyMap();
        Map<String, List<String>> result = new HashMap<>();
        for (String name : nameSet()) {
            result.put(name, values(name));
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Iterator<NameAndValue> iterator() {
        return new PairIterator();
    }

    final boolean equal(String str1, String str2) {
        return comparator.compare(str1, str2) == 0;
    }

    final String contentToString(String nameValueSeparator, String lineSeparator) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, size = names.size(); i < size; ++i) {
            if (i > 0) builder.append(lineSeparator);
            builder.append(names.get(i)).append(nameValueSeparator).append(values.get(i));
        }
        return builder.toString();
    }

    final MutablePair toMutablePair() {
        return new MutablePair(new ArrayList<>(names), new ArrayList<>(values), comparator);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair that = (Pair) o;

        if (!names.equals(that.names)) return false;
        if (!values.equals(that.values)) return false;
        return comparator.equals(that.comparator);
    }

    @Override
    public int hashCode() {
        int result = names.hashCode();
        result = 31 * result + values.hashCode();
        result = 31 * result + comparator.hashCode();
        return result;
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + '{' + contentToString("=", ", ") + '}';
    }


    class PairIterator implements Iterator<NameAndValue> {
        final Iterator<String> namesItr = names.iterator();
        final Iterator<String> valuesItr = values.iterator();

        @Override
        public final boolean hasNext() {
            boolean result = namesItr.hasNext() && valuesItr.hasNext();
            checkSize(names, values);
            return result;
        }

        @Override
        public final NameAndValue next() {
            NameAndValue result = new NameAndValue(namesItr.next(), valuesItr.next());
            checkSize(names, values);
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }

    static void checkSize(List<String> names, List<String> values) {
        int ns = names.size(), vs = values.size();
        if (ns != vs) {
            throw new IllegalArgumentException("names.size(" + ns + ") != values.size(" + vs + ')');
        }
    }
}
