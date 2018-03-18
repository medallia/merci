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
import com.medallia.merci.core.structure.Context;
import com.medallia.merci.core.structure.Modifiers;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Unit tests for {@link FeatureFlagManager}.
 */
public class FeatureFlagManagerTest {

    private static final String FEATURE_FLAG_NAME = "enable-none";

    private static final  String SINGLE_VALUE_FEATURE_FLAGS_JSON =
            "{\n" +
            "  \"feature-flags\" : {\n" +
            "    \"enable-none\" : {\n" +
            "      \"value\" : false\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final String SINGLE_VALUE_FEATURE_FLAGS_YAML =
            "---\n" +
            "feature-flags:\n" +
            "  enable-none:\n" +
            "    value: false";

    private static final String EMPTY_FEATURE_FLAGS_JSON = "{\n  \"feature-flags\" : { }\n}";

    private static final String EMPTY_FEATURE_FLAGS_YAML = "---\nfeature-flags: {}";

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ConfigurationWriter<Boolean> jsonConfigurationWriter = new ConfigurationWriter<>("feature-flags", jsonMapper);
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ConfigurationWriter<Boolean> yamlConfigurationWriter = new ConfigurationWriter<>("feature-flags", yamlMapper);

    private final ConfigurationContext qa = new EnvironmentConfigurationContext("qa");

    private final Map<String, Configuration<Boolean>> singleValueFeatureFlags = ImmutableMap.of(
            FEATURE_FLAG_NAME, new Configuration<>(FEATURE_FLAG_NAME, new Context<>(
                    Boolean.FALSE, null)));

    private final Map<String, Configuration<Boolean>> multiValueFeatureFlags = ImmutableMap.of(
            FEATURE_FLAG_NAME, new Configuration<>(FEATURE_FLAG_NAME, new Context<>(
                    Boolean.FALSE,
                    new Modifiers<>("environment", ImmutableMap.of(
                            "qa", new Context<>(Boolean.TRUE, null))))));

    private final FeatureFlagManager featureFlagManager = new FeatureFlagManager();

    @Test
    public void testIsActiveReturnsDefaultVaueForMissingFeatureFlag() {
        Assert.assertTrue(featureFlagManager.isActive(FEATURE_FLAG_NAME, qa, Boolean.TRUE));
        Assert.assertFalse(featureFlagManager.isActive(FEATURE_FLAG_NAME, qa, Boolean.FALSE));
        Assert.assertFalse(featureFlagManager.isActive(FEATURE_FLAG_NAME, qa));
    }

    @Test
    public void testIsActiveReturnsValueForProvidedRuntimeContext() {
        featureFlagManager.updateConfigurations(multiValueFeatureFlags);
        Assert.assertTrue(featureFlagManager.isActive(FEATURE_FLAG_NAME, qa));
    }

    @Test
    public void testGetConfigNamesReturnsCorrectNames() {
        featureFlagManager.updateConfigurations(multiValueFeatureFlags);
        Assert.assertEquals(Arrays.asList(FEATURE_FLAG_NAME), featureFlagManager.getConfigurationNames());
    }

    @Test
    public void testAsStringReturnsCorrectJson() throws IOException {
        featureFlagManager.updateConfigurations(singleValueFeatureFlags);
        String featureFlagsAsJson = featureFlagManager.asString(jsonConfigurationWriter);
        Assert.assertEquals(SINGLE_VALUE_FEATURE_FLAGS_JSON, featureFlagsAsJson);
    }

    @Test
    public void testAsStringForSingleConfigurationReturnsCorrectJson() throws IOException {
        featureFlagManager.updateConfigurations(singleValueFeatureFlags);
        String featureFlagsAsJson = featureFlagManager.asString(jsonConfigurationWriter, FEATURE_FLAG_NAME);
        Assert.assertEquals(SINGLE_VALUE_FEATURE_FLAGS_JSON, featureFlagsAsJson);
    }

    @Test
    public void testAsStringForMissingSingleConfigurationReturnsJson() throws IOException {
        String featureFlagsAsJson = featureFlagManager.asString(jsonConfigurationWriter, FEATURE_FLAG_NAME);
        Assert.assertEquals(EMPTY_FEATURE_FLAGS_JSON, featureFlagsAsJson);
    }

    @Test
    public void testAsStringReturnsCorrectYaml() throws IOException {
        featureFlagManager.updateConfigurations(singleValueFeatureFlags);
        String featureFlagsAsYaml = featureFlagManager.asString(yamlConfigurationWriter);
        Assert.assertEquals(SINGLE_VALUE_FEATURE_FLAGS_YAML, featureFlagsAsYaml);
    }

    @Test
    public void testAsStringForSingleConfigurationReturnsCorrectYaml() throws IOException {
        featureFlagManager.updateConfigurations(singleValueFeatureFlags);
        String featureFlagsAsYaml = featureFlagManager.asString(yamlConfigurationWriter, FEATURE_FLAG_NAME);
        Assert.assertEquals(SINGLE_VALUE_FEATURE_FLAGS_YAML, featureFlagsAsYaml);
    }

    @Test
    public void testAsStringForMissingSingleConfigurationReturnsYaml() throws IOException {
        String featureFlagsAsYaml = featureFlagManager.asString(yamlConfigurationWriter, FEATURE_FLAG_NAME);
        Assert.assertEquals(EMPTY_FEATURE_FLAGS_YAML, featureFlagsAsYaml);
    }
}
