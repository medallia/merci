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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * Writer for textual configuration content.
 */
public class ConfigurationWriter<T>  {

    private final ObjectMapper mapper;
    private final String root;

    /**
     * Creates configuration writer.
     *
     * @root root root field of configurations
     * @param mapper JSON deserializer, converts textual representation of config to config object (graph)
     */
    public ConfigurationWriter(String root, ObjectMapper mapper) {
        this.root = root;
        this.mapper = mapper;
    }

    /**
     * @return textual representation configurations, i.e. as JSON or YAML.
     *
     * @param configurations map with configurations
     * @throws IOException in case of failure
     */
    public String writeValueAsString(Map<String, Configuration<T>> configurations) throws IOException {
        Writer stringWriter = new StringWriter();
        JsonGenerator generator = mapper.getFactory().createGenerator(stringWriter);
        generator.useDefaultPrettyPrinter();
        generator.writeStartObject();
        generator.writeObjectFieldStart(root);
        for (Map.Entry<String, Configuration<T>> configuration : configurations.entrySet()) {
            generator.writeObjectField(configuration.getKey(), configuration.getValue().getContext());
        }
        generator.writeEndObject();
        generator.writeEndObject();
        generator.flush();
        return stringWriter.toString();
    }
}
