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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medallia.merci.core.metrics.InstantiateConfigurationMetrics;
import com.medallia.merci.core.structure.Context;
import com.medallia.merci.core.utils.ClassFinder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Parser for configurations.
 *
 * @param <T> type of configuration
 */
public class ConfigurationMapper<T> {

    private final ObjectMapper objectMapper;
    private final ConfigurationWriter<T> configurationWriter;
    private final String root;
    private final InstantiateConfigurationMetrics metrics;
    private final ClassFinder<T> classFinder;
    private final boolean skipNonInstantiable;

    /**
     * Creates ConfigurationMapper.
     *
     * @root root root field
     * @param objectMapper JSON deserializer
     */
    public ConfigurationMapper(String root,
                               boolean skipNonInstantiable,
                               ObjectMapper objectMapper,
                               InstantiateConfigurationMetrics metrics,
                               ClassFinder<T> classFinder) {
        this.root = root;
        this.objectMapper = objectMapper;
        this.skipNonInstantiable = skipNonInstantiable;
        this.metrics = metrics;
        this.classFinder = classFinder;
        configurationWriter = new ConfigurationWriter<>(root, objectMapper);
    }

    /**
     * Returns map of configurations, deserialized from provided textual configuration content.
     *
     * @param content textual configuration content
     * @return map of configurations
     * @throws IOException, if content could not be deserialized to map of configurations
     */
    public Map<String, Configuration<T>> readValue(String content) throws IOException {
        JsonNode rootJsonNode = objectMapper.readTree(content).get(root);
        if (rootJsonNode == null) {
            throw new IOException("Missing root field " + root);
        }
        return createConfigurations(rootJsonNode);
    }

    /**
     * Returns provided map of configurations as JSON String.
     *
     * @param configurations map of configurations to be serialized to JSON String
     * @throws IOException in case of JSON processing issues
     */
    public String writeValueAsString(Map<String, Configuration<T>> configurations) throws IOException {
        return configurationWriter.writeValueAsString(configurations);
    }

    /**
     * Returns new map of configurations from provided JsonNode configuration hierarchy.
     *
     * @param rootJsonNode root node of configuration hierarchy
     * @return new map of configurations
     * @throws IOException in case of conversion issues
     */
    private Map<String, Configuration<T>> createConfigurations(JsonNode rootJsonNode) throws IOException {
        Map<String, Configuration<T>> configurations = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> configurationEntries = rootJsonNode.fields();
        while (configurationEntries.hasNext()) {
            try {
                Map.Entry<String, JsonNode> configurationEntry = configurationEntries.next();
                String className = configurationEntry.getKey();
                Configuration<T> configuration = convertValue(configurationEntry.getValue(), className);
                configurations.put(className, configuration);
            } catch (IOException exception) {
                if (skipNonInstantiable) {
                    metrics.incrementNonInstantiableSkips();
                } else {
                    throw exception;
                }
            }
        }
        return configurations;
    }

    /**
     * Converts JsonNode structure to single configuration.
     *
     * @param json root JsonNode of single configuration
     * @param className name of class to be used for configuration value object
     * @return new single configuration
     * @throws IOException in case of conversion issues
     */
    private Configuration<T> convertValue(JsonNode json, String className) throws IOException {
        try {
            Class<T> clazz = classFinder.findClass(className);
            Context<T> context = convertValue(json, clazz);
            return new Configuration<>(className, context);
        } catch (ClassNotFoundException exception) {
            //non-instantiable configuration class
            throw new IOException(exception);
        }
    }

    /**
     * Converts JSON representation of configuration to a newly instantiated Java config object (graph) with root Context object.
     *
     * @param json JSON representation to be de-serialized
     * @param clazz class of target config object
     * @return newly instantiated object of type T
     * @throws IOException, if JavaType and JSON are incompatible or other deserialization problems
     */
    private Context<T> convertValue(JsonNode json, Class<T> clazz) throws IOException {
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructParametricType(Context.class, clazz);
            return objectMapper.convertValue(json, javaType);
        } catch (IllegalArgumentException exception) {
            //non-instantiable configuration class
            throw new IOException(exception);
        }
    }
}
