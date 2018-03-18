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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * Store for runtime JsonNode configurations.
 */
public class JsonConfigManager extends AbstractConfigurationManager<JsonNode> {

    private final JsonNode emptyConfiguration;

    /**
     * Creates new Json Node config manager.
     */
    public JsonConfigManager() {
        super();
        emptyConfiguration = new JsonNodeFactory(false).objectNode();
    }

    /**
     * Return JsonNode config object (graph) if config store contains config for given config name, empty config JsonNode otherwise.
     *
     * @param name name of configuration
     * @param runtimeContext context to be used for evaluation of JsonNode configuration
     * @return JsonNode configuration hierarchy
     */
    public JsonNode getConfig(String name, ConfigurationContext runtimeContext) {
        return getValue(name, runtimeContext, emptyConfiguration);
    }
}
