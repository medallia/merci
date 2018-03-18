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
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableMap;
import com.medallia.merci.core.common.EnvironmentConfigurationContext;
import com.medallia.merci.core.structure.Context;
import com.medallia.merci.core.structure.Modifiers;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class JsonConfigManagerTest {

    private static final String CONFIG_NAME = "com.medallia.merci.core.configs.NumberConfig";

    private static final  String SINGLE_VALUE_CONFIGS_JSON =
            "{\n" +
                    "  \"configs\" : {\n" +
                    "    \"com.medallia.merci.core.configs.NumberConfig\" : {\n" +
                    "      \"value\" : {\n" +
                    "        \"number\" : 1\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";

    private static final String SINGLE_VALUE_CONFIGS_YAML =
            "---\n" +
                    "configs:\n" +
                    "  com.medallia.merci.core.configs.NumberConfig:\n" +
                    "    value:\n" +
                    "      number: 1";

    private static final String EMPTY_CONFIGS_JSON = "{\n  \"configs\" : { }\n}";

    private static final String EMPTY_CONFIGS_YAML = "---\nconfigs: {}";

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final JsonNodeFactory jsonNodeFactory = jsonMapper.getNodeFactory();
    private final ConfigurationWriter<JsonNode> jsonConfigurationWriter = new ConfigurationWriter<>("configs", jsonMapper);
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ConfigurationWriter<JsonNode> yamlConfigurationWriter = new ConfigurationWriter<>("configs", yamlMapper);

    private final ConfigurationContext qa = new EnvironmentConfigurationContext("qa");

    private final Map<String, Configuration<JsonNode>> singleValueConfigs = ImmutableMap.of(
            CONFIG_NAME, new Configuration<>(CONFIG_NAME, new Context<>(
                    new ObjectNode(jsonNodeFactory, ImmutableMap.of("number", new IntNode(1))),
                    null)));

    private final Map<String, Configuration<JsonNode>> multiValueConfigs = ImmutableMap.of(
            CONFIG_NAME, new Configuration<>(CONFIG_NAME, new Context<>(
                    new ObjectNode(jsonNodeFactory, ImmutableMap.of("number", new IntNode(1))),
                    new Modifiers<>("environment", ImmutableMap.of(
                            "qa", new Context<>(
                                    new ObjectNode(jsonNodeFactory, ImmutableMap.of("number", new IntNode(2))),
                                    null))))));

    private final JsonConfigManager jsonConfigManager = new JsonConfigManager();

    @Test
    public void testGetConfigReturnsEmptyJsonNodeForMissingConfig() {
        JsonNode config = jsonConfigManager.getConfig(CONFIG_NAME, qa);
        Assert.assertEquals(0, config.size());
    }

    @Test
    public void testGetConfigReturnsConfigForProvidedRuntimeContext() {
        jsonConfigManager.updateConfigurations(multiValueConfigs);
        JsonNode config = jsonConfigManager.getConfig(CONFIG_NAME, qa);
        Assert.assertEquals(2, config.get("number").asInt());
    }

    @Test
    public void testGetConfigNamesReturnsCorrectNames() {
        jsonConfigManager.updateConfigurations(multiValueConfigs);
        Assert.assertEquals(Arrays.asList(CONFIG_NAME), jsonConfigManager.getConfigurationNames());
    }

    @Test
    public void testAsStringReturnsCorrectJson() throws IOException {
        jsonConfigManager.updateConfigurations(singleValueConfigs);
        String configsAsJson = jsonConfigManager.asString(jsonConfigurationWriter);
        Assert.assertEquals(SINGLE_VALUE_CONFIGS_JSON, configsAsJson);
    }

    @Test
    public void testAsStringForSingleConfigurationReturnsCorrectJson() throws IOException {
        jsonConfigManager.updateConfigurations(singleValueConfigs);
        String configsAsJson = jsonConfigManager.asString(jsonConfigurationWriter, CONFIG_NAME);
        Assert.assertEquals(SINGLE_VALUE_CONFIGS_JSON, configsAsJson);
    }

    @Test
    public void testAsStringForMissingSingleConfigurationReturnsJson() throws IOException {
        String configsAsJson = jsonConfigManager.asString(jsonConfigurationWriter, CONFIG_NAME);
        Assert.assertEquals(EMPTY_CONFIGS_JSON, configsAsJson);
    }

    @Test
    public void testAsStringReturnsCorrectYaml() throws IOException {
        jsonConfigManager.updateConfigurations(singleValueConfigs);
        String configsAsYaml = jsonConfigManager.asString(yamlConfigurationWriter);
        Assert.assertEquals(SINGLE_VALUE_CONFIGS_YAML, configsAsYaml);
    }

    @Test
    public void testAsStringForSingleConfigurationReturnsCorrectYaml() throws IOException {
        jsonConfigManager.updateConfigurations(singleValueConfigs);
        String configsAsYaml = jsonConfigManager.asString(yamlConfigurationWriter, CONFIG_NAME);
        Assert.assertEquals(SINGLE_VALUE_CONFIGS_YAML, configsAsYaml);
    }

    @Test
    public void testAsStringForMissingSingleConfigurationReturnsYaml() throws IOException {
        String configsAsYaml = jsonConfigManager.asString(yamlConfigurationWriter, CONFIG_NAME);
        Assert.assertEquals(EMPTY_CONFIGS_YAML, configsAsYaml);
    }
}