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
package com.medallia.merci.web.configs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Config.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {

    private final String configId;
    private final JsonNode value;

    @JsonCreator
    public Config(@JsonProperty(value = "id", required = true) String configId,
                  @JsonProperty(value = "value", required = true) JsonNode value) {
        this.configId = configId;
        this.value = value;
    }

    @JsonProperty("id")
    public String getId() {
        return configId;
    }

    @JsonProperty("value")
    public JsonNode getValue() {
        return value;
    }
}
