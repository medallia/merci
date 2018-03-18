/*
 * Copyright 2018 Medallia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.medallia.merci.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Generic configuration context, representing context type to value mappings, i.e. "environment" -> "qa".
 */
public class ConfigurationContext {

    private final Map<String, String> contextMap;

    /**
     * Creates empty configuration context.
     */
    public ConfigurationContext() {
        contextMap = new LinkedHashMap<>();
    }

    /**
     * Inserts or updates a context type to value mapping, i.e. "environment" -> "qa".
     *
     * @param type type of mapping
     * @param value value of mapping
     */
    public void put(String type, String value) {
        contextMap.put(type, value);
    }

    /**
     * Returns context value associated with provided context type, or null if none found.
     *
     * @param type name of context type
     * @return context-value, null if none found
     */
    public String get(String type) {
        return contextMap.get(type);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return Objects.equals(contextMap, ((ConfigurationContext) other).contextMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contextMap);
    }
}
