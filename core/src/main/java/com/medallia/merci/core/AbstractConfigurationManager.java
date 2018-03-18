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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract class of in-memory store for configurations.
 *
 * @param <T> type of configuration to be stored
 */
public abstract class AbstractConfigurationManager<T> implements ConfigurationManager<T> {

    private final AtomicReference<Map<String, Configuration<T>>> configurationStore;

    /** Protected constructor. */
    protected AbstractConfigurationManager() {
        configurationStore = new AtomicReference<>(new HashMap<>());
    }

    @Override
    public void updateConfigurations(Map<String, Configuration<T>> configurations) {
        configurationStore.set(configurations);
    }

    /**
     * @return list of configuration names from configuration store.
     */
    public List<String> getConfigurationNames() {
        return new ArrayList<>(configurationStore.get().keySet());
    }

    /**
     * Returns configuration store as JSON String.
     *
     * @param writer configuration writer to be used for serialization
     * @return JSON of serialized configuration store
     * @throws IOException in case of JSON processing issues
     */
    public String asString(ConfigurationWriter<T> writer) throws IOException {
        return writer.writeValueAsString(configurationStore.get());
    }

    /**
     * Returns configuration as JSON String.
     *
     * @param writer configuration writer to be used for serialization
     * @param configName name of configuration to be serialized by configuration writer
     * @return JSON of serialized configuration
     * @throws IOException in case of JSON processing issues
     */
    public String asString(ConfigurationWriter<T> writer, String configName) throws IOException {
        Configuration<T> configuration = configurationStore.get().get(configName);
        if (configuration == null) {
            return writer.writeValueAsString(Collections.emptyMap());
        }
        Map<String, Configuration<T>> map = new HashMap<>();
        map.put(configuration.getName(), configuration);
        return writer.writeValueAsString(map);
    }

    /**
     * Return configuration value object of type T with provided name from configuration store
     * for given runtime context, default value if no configuration with provided name found.
     *
     * @param name name of configuration to be evaluated
     * @param runtimeContext context at runtime
     * @param defaultValue value to be returned, if no configuration with provided name found
     * @return configuration value object
     */
    protected T getValue(String name, ConfigurationContext runtimeContext, T defaultValue) {
        Configuration<T> configuration = configurationStore.get().get(name);
        if (configuration != null) {
            return configuration.getValue(runtimeContext);
        }
        return defaultValue;
    }
}
