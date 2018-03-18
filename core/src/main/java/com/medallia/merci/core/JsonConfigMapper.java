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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medallia.merci.core.metrics.InstantiateConfigurationMetrics;

/**
 * Parser for JsonNode configurations.
 */
public class JsonConfigMapper extends ConfigurationMapper<JsonNode> {

    /**
     * Creates JsonConfigMapper.
     *
     * @root root root field of JsonNode configurations
     * @param objectMapper JSON deserializer, converts textual representation of config to JsonNode config object (graph)
     */
    public JsonConfigMapper(String root,
                            boolean skipNonInstantiable,
                            ObjectMapper objectMapper,
                            InstantiateConfigurationMetrics metrics) {
        super(root, skipNonInstantiable, objectMapper, metrics, className -> JsonNode.class);
    }
}
