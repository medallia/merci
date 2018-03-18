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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableMap;
import com.medallia.merci.core.common.EnvironmentConfigurationContext;
import com.medallia.merci.core.configs.AbstractClassConfig;
import com.medallia.merci.core.configs.MessageConfig;
import com.medallia.merci.core.configs.NumberConfig;
import com.medallia.merci.core.exception.ConfigInstantiationException;
import com.medallia.merci.core.structure.Context;
import com.medallia.merci.core.structure.Modifiers;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Unit tests for {@link ConfigManager}.
 */
public class ConfigManagerTest {

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
    private final ConfigurationWriter<Object> jsonConfigurationWriter = new ConfigurationWriter<>("configs", jsonMapper);
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ConfigurationWriter<Object> yamlConfigurationWriter = new ConfigurationWriter<>("configs", yamlMapper);

    private final ConfigurationContext qa = new EnvironmentConfigurationContext("qa");

    @SuppressWarnings("unchecked")
    private final Map<String, Configuration<Object>> singleValueConfigs = ImmutableMap.of(
            CONFIG_NAME, new Configuration(CONFIG_NAME, new Context<>(
                    new NumberConfig(1), null)));

    @SuppressWarnings("unchecked")
    private final Map<String, Configuration<Object>> multiValueConfigs = ImmutableMap.of(
            CONFIG_NAME, new Configuration(CONFIG_NAME, new Context<>(
                    new NumberConfig(1),
                    new Modifiers<>("environment", ImmutableMap.of(
                            "qa", new Context<>(new NumberConfig(2), null))))));

    @SuppressWarnings("unchecked")
    private final Map<String, Configuration<Object>> wrongMappingConfigs = ImmutableMap.of(
            CONFIG_NAME, new Configuration(CONFIG_NAME, new Context<>(
                    new MessageConfig("Wrong config"), null)));

    private final ConfigManager configManager = new ConfigManager();

    @Test
    public void testGetConfigReturnsHardcodedDefaultConfigForMissingConfig() {
        configManager.updateConfigurations(multiValueConfigs);
        MessageConfig config = configManager.getConfig(MessageConfig.class, qa);
        Assert.assertEquals("invalid, hardcoded config", config.getMessage());
    }

    @Test(expected = ConfigInstantiationException.class)
    public void testGetConfigThrowsConfigInstantiationExceptionForAbstractConfigClass() {
        configManager.getConfig(AbstractClassConfig.class, qa);
    }

    @Test(expected = ConfigInstantiationException.class)
    public void testGetConfigThrowsConfigInstantiationExceptionForJavaLangClass() {
        configManager.getConfig(Class.class, qa);
    }

    @Test
    public void testGetConfigReturnsConfigForProvidedRuntimeContext() {
        configManager.updateConfigurations(multiValueConfigs);
        NumberConfig config = configManager.getConfig(NumberConfig.class, qa);
        Assert.assertEquals(2, config.getNumber());
    }

    @Test
    public void testGetConfigReturnsReturnsHardcodedDefaultConfigForWrongConfigMapping() {
        configManager.updateConfigurations(wrongMappingConfigs);
        NumberConfig config = configManager.getConfig(NumberConfig.class, qa);
        Assert.assertEquals(-1, config.getNumber());
    }

    @Test
    public void testGetConfigNamesReturnsCorrectNames() {
        configManager.updateConfigurations(multiValueConfigs);
        Assert.assertEquals(Arrays.asList(CONFIG_NAME), configManager.getConfigurationNames());
    }

    @Test
    public void testAsStringReturnsCorrectJson() throws IOException {
        configManager.updateConfigurations(singleValueConfigs);
        String configsAsJson = configManager.asString(jsonConfigurationWriter);
        Assert.assertEquals(SINGLE_VALUE_CONFIGS_JSON, configsAsJson);
    }

    @Test
    public void testAsStringForSingleConfigurationReturnsCorrectJson() throws IOException {
        configManager.updateConfigurations(singleValueConfigs);
        String configsAsJson = configManager.asString(jsonConfigurationWriter, CONFIG_NAME);
        Assert.assertEquals(SINGLE_VALUE_CONFIGS_JSON, configsAsJson);
    }

    @Test
    public void testAsStringForMissingSingleConfigurationReturnsJson() throws IOException {
        String configsAsJson = configManager.asString(jsonConfigurationWriter, CONFIG_NAME);
        Assert.assertEquals(EMPTY_CONFIGS_JSON, configsAsJson);
    }

    @Test
    public void testAsStringReturnsCorrectYaml() throws IOException {
        configManager.updateConfigurations(singleValueConfigs);
        String configsAsYaml = configManager.asString(yamlConfigurationWriter);
        Assert.assertEquals(SINGLE_VALUE_CONFIGS_YAML, configsAsYaml);
    }

    @Test
    public void testAsStringForSingleConfigurationReturnsCorrectYaml() throws IOException {
        configManager.updateConfigurations(singleValueConfigs);
        String configsAsYaml = configManager.asString(yamlConfigurationWriter, CONFIG_NAME);
        Assert.assertEquals(SINGLE_VALUE_CONFIGS_YAML, configsAsYaml);
    }

    @Test
    public void testAsStringForMissingSingleConfigurationReturnsYaml() throws IOException {
        String configsAsYaml = configManager.asString(yamlConfigurationWriter, CONFIG_NAME);
        Assert.assertEquals(EMPTY_CONFIGS_YAML, configsAsYaml);
    }
}