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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
final class MutablePair extends Pair implements PairWriter {
    MutablePair(List<String> names, List<String> values, Comparator<String> comparator) {
        super(names, values, comparator);
    }

    @Override
    public void add(String name, String value) {
        this.names.add(name);
        this.values.add(value);
    }

    @Override
    public void addIfNot(String name, String value) {
        if (!contains(name)) {
            add(name, value);
        }
    }

    @Override
    public void addAll(List<String> names, List<String> values) {
        checkSize(names, values);
        this.names.addAll(names);
        this.values.addAll(values);
    }

    @Override
    public void set(String name, String value) {
        removeAll(name);
        add(name, value);
    }

    @Override
    public void replaceIfExists(String name, String value) {
        if (contains(name)) {
            set(name, value);
        }
    }

    @Override
    public void removeAll(String name) {
        for (int i = names.size() - 1; i >= 0; --i) {
            if (equal(name, names.get(i))) {
                names.remove(i);
                values.remove(i);
            }
        }
    }

    @Override
    public void clear() {
        names.clear();
        values.clear();
    }

    @Override
    public Iterator<NameAndValue> iterator() {
        return new MutablePairIterator();
    }

    final Pair toPair() {
        return new Pair(new ArrayList<>(names), new ArrayList<>(values), comparator);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MutablePair pair = (MutablePair) o;
        return Objects.equals(names, pair.names) &&
                Objects.equals(values, pair.values) &&
                Objects.equals(comparator, pair.comparator);
    }

    @Override
    public int hashCode() {
        return 17 * Objects.hash(names, values, comparator);
    }


    private class MutablePairIterator extends PairIterator {
        @Override
        public void remove() {
            checkSize(names, values);
            namesItr.remove();
            valuesItr.remove();
            checkSize(names, values);
        }
    }
}
