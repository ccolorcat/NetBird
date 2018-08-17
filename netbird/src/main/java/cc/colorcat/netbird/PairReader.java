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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: cxx
 * Date: 2018-8-17
 * GitHub: https://github.com/ccolorcat
 */
public interface PairReader extends Iterable<NameAndValue> {

    int size();

    boolean isEmpty();

    boolean contains(String name);

    List<String> names();

    List<String> values();

    String name(int index);

    String value(int index);

    String value(String name);

    String value(String name, String defaultValue);

    List<String> values(String name);

    Set<String> nameSet();

    Map<String, List<String>> toMultimap();
}
