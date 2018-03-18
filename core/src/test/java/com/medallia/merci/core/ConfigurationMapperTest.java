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
import com.google.common.collect.ImmutableMap;
import com.medallia.merci.core.common.EnvironmentConfigurationContext;
import com.medallia.merci.core.configs.AbstractClassConfig;
import com.medallia.merci.core.configs.NumberConfig;
import com.medallia.merci.core.metrics.ConfigMetrics;
import com.medallia.merci.core.structure.Context;
import com.medallia.merci.core.utils.DefaultClassFinder;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Unit tests for {@link ConfigurationMapper}.
 */
public class ConfigurationMapperTest {

    private static final String NUMBER_CONFIG_NAME = "com.medallia.merci.core.configs.NumberConfig";

    private static final  String SINGLE_VALUE_CONFIGS_JSON = "{\n" +
            "  \"configs\" : {\n" +
            "    \"com.medallia.merci.core.configs.NumberConfig\" : {\n" +
            "      \"value\" : {\n" +
            "        \"number\" : 1\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final  String ABSTRACT_CLASS_CONFIGS_JSON = "{\n" +
            "  \"configs\" : {\n" +
            "    \"com.medallia.merci.core.configs.AbstractClassConfig\" : {\n" +
            "      \"value\" : {\n" +
            "        \"timeout\" : 60\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final  String MISSING_CLASS_CONFIGS_JSON = "{\n" +
            "  \"configs\" : {\n" +
            "    \"com.medallia.merci.core.configs.MissingClassConfig\" : {\n" +
            "      \"value\" : {\n" +
            "        \"timeout\" : 60\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final String MULTI_VALUE_CONFIGS_JSON = "{\n" +
            "  \"configs\" : {\n" +
            "    \"com.medallia.merci.core.configs.NumberConfig\" : {\n" +
            "      \"value\" : {\n" +
            "        \"number\" : 1\n" +
            "      },\n" +
            "      \"modifiers\" : {\n" +
            "        \"type\" : \"environment\",\n" +
            "        \"contexts\" : {\n" +
            "          \"qa\" : {\n" +
            "            \"value\" : {\n" +
            "              \"number\" : 2\n" +
            "            }\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final String EMPTY_CONFIGS_JSON = "{\n  \"configs\" : { }\n}";

    private final Map<String, Configuration<NumberConfig>> singleValueConfigs = ImmutableMap.of(
            NUMBER_CONFIG_NAME, new Configuration<>(NUMBER_CONFIG_NAME, new Context<>(
                    new NumberConfig(1), null)));

    private final Map<String, Configuration<NumberConfig>> emptyValueConfigs = Collections.emptyMap();

    private final ObjectMapper jsonMapper = new ObjectMapper();

    private final ConfigurationContext none = new ConfigurationContext();
    private final ConfigurationContext qa = new EnvironmentConfigurationContext("qa");

    private final ConfigMetrics configMetrics = new ConfigMetrics();

    @Test(expected = IOException.class)
    public void testReadValueThrowsIOExceptionIfNoRootField() throws IOException {
        ConfigurationMapper<NumberConfig> configurationMapper = new ConfigurationMapper<>("configs", true, jsonMapper,
                configMetrics, className -> NumberConfig.class);
        configurationMapper.readValue("{  \"something\" : { }}");
    }

    @Test
    public void testReadValueAsJsonConfigReturnsCorrectMapOfConfigurationsForAbstractConfigClass() throws IOException {
        ConfigurationMapper<JsonNode> configurationMapper = new JsonConfigMapper("configs", false, jsonMapper, configMetrics);
        configurationMapper.readValue(ABSTRACT_CLASS_CONFIGS_JSON);
        Assert.assertEquals(0, configMetrics.getConfigNonInstantiableSkips());
    }

    @Test(expected = IOException.class)
    public void testReadValueThrowsIOExceptionForAbstractConfigClass() throws IOException {
        ConfigurationMapper<AbstractClassConfig> configurationMapper = new ConfigurationMapper<>("configs", false, jsonMapper,
                configMetrics, className -> AbstractClassConfig.class);
        configurationMapper.readValue(ABSTRACT_CLASS_CONFIGS_JSON);
    }

    @Test
    public void testReadValueSkipsConfigurationForAbstractConfigClass() throws IOException {
        ConfigurationMapper<AbstractClassConfig> configurationMapper = new ConfigurationMapper<>("configs", true, jsonMapper,
                configMetrics, className -> AbstractClassConfig.class);
        configurationMapper.readValue(ABSTRACT_CLASS_CONFIGS_JSON);
        Assert.assertEquals(0, configMetrics.getConfigUpdates());
        Assert.assertEquals(1, configMetrics.getConfigNonInstantiableSkips());
    }

    @Test(expected = IOException.class)
    public void testReadValueThrowsIOExceptionForMissingConfigClass() throws IOException {
        ConfigurationMapper<Object> configurationMapper = new ConfigurationMapper<>("configs", false, jsonMapper,
                configMetrics, new DefaultClassFinder());
        configurationMapper.readValue(MISSING_CLASS_CONFIGS_JSON);
    }

    @Test
    public void testReadValueSkipsConfigurationForMissingConfigClass() throws IOException {
        ConfigurationMapper<Object> configurationMapper = new ConfigurationMapper<>("configs", true, jsonMapper,
                configMetrics, new DefaultClassFinder());
        configurationMapper.readValue(MISSING_CLASS_CONFIGS_JSON);
        Assert.assertEquals(1, configMetrics.getConfigNonInstantiableSkips());
    }

    @Test
    public void testReadValueReturnsCorrectMapOfConfigurations() throws IOException {
        ConfigurationMapper<NumberConfig> configurationMapper = new ConfigurationMapper<>("configs", true, jsonMapper,
                configMetrics, className -> NumberConfig.class);
        Map<String, Configuration<NumberConfig>> configurations = configurationMapper.readValue(MULTI_VALUE_CONFIGS_JSON);
        Configuration<NumberConfig> configuration = configurations.get(NUMBER_CONFIG_NAME);
        Assert.assertEquals(1, configuration.getValue(none).getNumber());
        Assert.assertEquals(2, configuration.getValue(qa).getNumber());
    }

    @Test
    public void testWriteValueAsStringReturnsCorrectJson() throws IOException {
        ConfigurationMapper<NumberConfig> configurationMapper = new ConfigurationMapper<>("configs", true, jsonMapper,
                configMetrics, className -> NumberConfig.class);
        String configsAsJson = configurationMapper.writeValueAsString(singleValueConfigs);
        Assert.assertEquals(SINGLE_VALUE_CONFIGS_JSON, configsAsJson);
    }

    @Test
    public void testWriteValueAsStringForEmptyConfigurationsMapReturnsCorrectJson() throws IOException {
        ConfigurationMapper<NumberConfig> configurationMapper = new ConfigurationMapper<>("configs", true, jsonMapper,
                configMetrics, className -> NumberConfig.class);
        String configsAsJson = configurationMapper.writeValueAsString(emptyValueConfigs);
        Assert.assertEquals(EMPTY_CONFIGS_JSON, configsAsJson);
    }
}