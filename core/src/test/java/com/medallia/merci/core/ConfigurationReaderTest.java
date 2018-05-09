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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.medallia.merci.core.common.EnvironmentConfigurationContext;
import com.medallia.merci.core.fetcher.ConfigurationFetcher;
import com.medallia.merci.core.metrics.FeatureFlagMetrics;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

/**
 * Unit tests for {@link ConfigurationReader}.
 */
public class ConfigurationReaderTest {

    private static final String APPLICATION = "myapp-configurations";

    private static final String FIRST_FILE = "/first-featureflags.json";

    private static final  String FIRST_JSON = "{\n" +
            "  \"feature-flags\": {\n" +
            "    \"enable-feature-all\": {\n" +
            "      \"value\": true\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final String SECOND_FILE = "/second-featureflags.json";

    private static final  String SECOND_JSON = "{\n" +
            "  \"feature-flags\": {\n" +
            "    \"enable-feature-none\": {\n" +
            "      \"value\": false\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final  String BAD_SECOND_JSON = "{\n" +
            "  \"feature-flags\": {\n" +
            "    \"enable-feature-none\": {\n" +
            "      \"values\": false\n" +
            "    },\n" +
            "  }\n" +
            "}";

    private static final  String DUPLICATE_SECOND_JSON = "{\n" +
            "  \"feature-flags\": {\n" +
            "    \"enable-feature-all\": {\n" +
            "      \"value\": true\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private final ConfigurationContext qa = new EnvironmentConfigurationContext("qa");

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FeatureFlagManager featureFlagManager = new FeatureFlagManager();
    private final MessageDigest digest = createMessageDigest();

    private final FeatureFlagMetrics featureFlagMetrics = new FeatureFlagMetrics();

    @Test
    public void testRegular() throws IOException {
        ConfigurationFetcher configurationFetcher = (fileNames, application) ->
                ImmutableMap.of(FIRST_FILE, FIRST_JSON, SECOND_FILE, SECOND_JSON);

        ConfigurationMapper<Boolean> featureFlagMapper = new FeatureFlagMapper("feature-flags", true, objectMapper, featureFlagMetrics);

        ConfigurationReader<Boolean> configurationReader = new ConfigurationReader<>(APPLICATION, ImmutableList.of(FIRST_FILE, SECOND_FILE),
                configurationFetcher, featureFlagMapper, featureFlagManager, digest, featureFlagMetrics, 0);

        configurationReader.execute();

        Assert.assertEquals(ImmutableList.of("enable-feature-all", "enable-feature-none"), featureFlagManager.getConfigurationNames());
        Assert.assertTrue(featureFlagManager.isActive("enable-feature-all", qa));
        Assert.assertFalse(featureFlagManager.isActive("enable-feature-none", qa));
        Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagSameContentsSkips());
        Assert.assertEquals(1, featureFlagMetrics.getFeatureFlagNewContentsUpdates());
        Assert.assertEquals(2, featureFlagMetrics.getFeatureFlagUpdates());
        Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagContentFailures());
        Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagNameDuplicates());
    }

    @Test
    public void testSameContent() throws IOException {
        ConfigurationFetcher configurationFetcher = (fileNames, application) ->
                ImmutableMap.of(FIRST_FILE, FIRST_JSON, SECOND_FILE, SECOND_JSON);

        ConfigurationMapper<Boolean> featureFlagMapper = new FeatureFlagMapper("feature-flags", true, objectMapper, featureFlagMetrics);

        ConfigurationReader<Boolean> configurationReader = new ConfigurationReader<>(APPLICATION, ImmutableList.of(FIRST_FILE, SECOND_FILE),
                configurationFetcher, featureFlagMapper, featureFlagManager, digest, featureFlagMetrics, 1);

        configurationReader.execute();
        configurationReader.execute();

        Assert.assertEquals(ImmutableList.of("enable-feature-all", "enable-feature-none"), featureFlagManager.getConfigurationNames());
        Assert.assertTrue(featureFlagManager.isActive("enable-feature-all", qa));
        Assert.assertFalse(featureFlagManager.isActive("enable-feature-none", qa));
        Assert.assertEquals(1, featureFlagMetrics.getFeatureFlagSameContentsSkips());
        Assert.assertEquals(1, featureFlagMetrics.getFeatureFlagNewContentsUpdates());
        Assert.assertEquals(2, featureFlagMetrics.getFeatureFlagUpdates());
        Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagContentFailures());
        Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagNameDuplicates());
    }

    @Test
    public void testSameContentWithReset() throws IOException {
        ConfigurationFetcher configurationFetcher = (fileNames, application) ->
                ImmutableMap.of(FIRST_FILE, FIRST_JSON, SECOND_FILE, SECOND_JSON);

        ConfigurationMapper<Boolean> featureFlagMapper = new FeatureFlagMapper("feature-flags", true, objectMapper, featureFlagMetrics);

        ConfigurationReader<Boolean> configurationReader = new ConfigurationReader<>(APPLICATION, ImmutableList.of(FIRST_FILE, SECOND_FILE),
                configurationFetcher, featureFlagMapper, featureFlagManager, digest, featureFlagMetrics, 1);

        configurationReader.execute();
        configurationReader.reset();
        configurationReader.execute();

        Assert.assertEquals(ImmutableList.of("enable-feature-all", "enable-feature-none"), featureFlagManager.getConfigurationNames());
        Assert.assertTrue(featureFlagManager.isActive("enable-feature-all", qa));
        Assert.assertFalse(featureFlagManager.isActive("enable-feature-none", qa));
        Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagSameContentsSkips());
        Assert.assertEquals(2, featureFlagMetrics.getFeatureFlagNewContentsUpdates());
        Assert.assertEquals(4, featureFlagMetrics.getFeatureFlagUpdates());
        Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagContentFailures());
        Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagNameDuplicates());
    }

    @Test(expected = IOException.class)
    public void testBadFeatureFlagConfigurationContentResultsInIOException() throws IOException {
        ConfigurationFetcher configurationFetcher = (fileNames, application) ->
                ImmutableMap.of(FIRST_FILE, FIRST_JSON, SECOND_FILE, BAD_SECOND_JSON);

        ConfigurationMapper<Boolean> featureFlagMapper = new FeatureFlagMapper("feature-flags", true, objectMapper, featureFlagMetrics);

        ConfigurationReader<Boolean> configurationReader = new ConfigurationReader<>(APPLICATION, ImmutableList.of(FIRST_FILE, SECOND_FILE),
                configurationFetcher, featureFlagMapper, featureFlagManager, digest, featureFlagMetrics, 0);

        try {
            configurationReader.execute();
        } catch (IOException exception) {
            Assert.assertEquals(Collections.emptyList(), featureFlagManager.getConfigurationNames());
            Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagSameContentsSkips());
            Assert.assertEquals(1, featureFlagMetrics.getFeatureFlagNewContentsUpdates());
            Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagUpdates());
            Assert.assertEquals(1, featureFlagMetrics.getFeatureFlagContentFailures());
            Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagNameDuplicates());
            Assert.assertFalse(featureFlagManager.isActive("enable-feature-all", qa));
            throw exception;
        }
        Assert.fail("IOException should have been thrown.");
    }

    @Test(expected = IOException.class)
    public void testMismatchConfigurationRootResultsInIOException() throws IOException {
        ConfigurationFetcher configurationFetcher = (fileNames, application) ->
                ImmutableMap.of(FIRST_FILE, FIRST_JSON, SECOND_FILE, SECOND_JSON);

        ConfigurationMapper<Boolean> featureFlagMapper = new FeatureFlagMapper("flags", true, objectMapper, featureFlagMetrics);

        ConfigurationReader<Boolean> configurationReader = new ConfigurationReader<>(APPLICATION, ImmutableList.of(FIRST_FILE, SECOND_FILE),
                configurationFetcher, featureFlagMapper, featureFlagManager, digest, featureFlagMetrics, 0);

        try {
            configurationReader.execute();
        } catch (IOException exception) {
            Assert.assertEquals(Collections.emptyList(), featureFlagManager.getConfigurationNames());
            Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagSameContentsSkips());
            Assert.assertEquals(1, featureFlagMetrics.getFeatureFlagNewContentsUpdates());
            Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagUpdates());
            Assert.assertEquals(2, featureFlagMetrics.getFeatureFlagContentFailures());
            Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagNameDuplicates());
            throw exception;
        }
        Assert.fail("IOException should have been thrown.");
    }

    @Test
    public void testDuplicate() throws IOException {
        ConfigurationFetcher configurationFetcher = (fileNames, application) ->
                ImmutableMap.of(FIRST_FILE, FIRST_JSON, SECOND_FILE, DUPLICATE_SECOND_JSON);

        ConfigurationMapper<Boolean> featureFlagMapper = new FeatureFlagMapper("feature-flags", true, objectMapper, featureFlagMetrics);

        ConfigurationReader<Boolean> configurationReader = new ConfigurationReader<>(APPLICATION, ImmutableList.of(FIRST_FILE, SECOND_FILE),
                configurationFetcher, featureFlagMapper, featureFlagManager, digest, featureFlagMetrics, 0);

        configurationReader.execute();

        Assert.assertEquals(ImmutableList.of("enable-feature-all"), featureFlagManager.getConfigurationNames());
        Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagSameContentsSkips());
        Assert.assertEquals(1, featureFlagMetrics.getFeatureFlagNewContentsUpdates());
        Assert.assertEquals(1, featureFlagMetrics.getFeatureFlagUpdates());
        Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagContentFailures());
        Assert.assertEquals(1, featureFlagMetrics.getFeatureFlagNameDuplicates());
    }

    private static MessageDigest createMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException(exception);
        }
    }
}