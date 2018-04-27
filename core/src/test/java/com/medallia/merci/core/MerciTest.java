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
import com.medallia.merci.core.fetcher.ConfigurationFetcher;
import com.medallia.merci.core.metrics.ConfigMetrics;
import com.medallia.merci.core.metrics.ConfigurationLoaderMetrics;
import com.medallia.merci.core.metrics.FeatureFlagMetrics;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link Merci}.
 */
public class MerciTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MessageDigest digest = sha256();

    @Test
    public void testLoaderLoadsConfigurations() {
        ConfigurationFetcher fetcher = (fileNames, application) -> {
            if ("myapp".equals(application) && ImmutableList.of("/a-featureflags.json", "/b-featureflags.json").equals(fileNames)) {
                return ImmutableMap.of("/a-featureflags.json", "{ \"feature-flags\": { \"enable-all\": { \"value\": true } } }",
                        "/b-featureflags.json", "{ \"feature-flags\": { \"enable-none\": { \"value\": false } } }");
            } else if ("myapp".equals(application) && ImmutableList.of("/configs.json").equals(fileNames)) {
                return ImmutableMap.of("/configs.json", "{ \"configs\": { \"com.medallia.merci.core.configs.NumberConfig\": { \"value\" : { \"number\" : 1 } } } }");
            }
            return Collections.emptyMap();
        };

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        ScheduledExecutorService executorService = Mockito.mock(ScheduledExecutorService.class);

        Merci merci = new Merci(fetcher, executorService, objectMapper, digest);
        FeatureFlagMetrics featureFlagMetrics = new FeatureFlagMetrics();

        FeatureFlagManager featureFlagManager = merci.addFeatureFlagManager("myapp").registerFile("/a-featureflags.json").registerFile("/b-featureflags.json")
                .setMetrics(featureFlagMetrics).build();
        ConfigMetrics configMetrics = new ConfigMetrics();
        ConfigManager configManager = merci.addConfigManager("myapp").registerFile("/configs.json").setMetrics(configMetrics).build();

        ConfigurationLoader loader = merci.createLoader(Duration.ofSeconds(10));
        loader.start();

        Mockito.verify(executorService, Mockito.times(1)).scheduleWithFixedDelay(runnableCaptor.capture(), Matchers.anyLong(), Matchers.anyLong(), Matchers.any(TimeUnit.class));
        runnableCaptor.getAllValues().get(0).run();
        Assert.assertEquals(ImmutableList.of("enable-all", "enable-none"), featureFlagManager.getConfigurationNames());
        Assert.assertEquals(ImmutableList.of("com.medallia.merci.core.configs.NumberConfig"), configManager.getConfigurationNames());
        Assert.assertEquals(2, featureFlagMetrics.getFeatureFlagUpdates());
        Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagContentFailures());
        Assert.assertEquals(1, featureFlagMetrics.getFeatureFlagNewContentsUpdates());
        Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagSameContentsSkips());
        Assert.assertEquals(1, configMetrics.getConfigUpdates());
        Assert.assertEquals(0, configMetrics.getConfigNonInstantiableSkips());
        Assert.assertEquals(0, configMetrics.getConfigContentFailures());
        Assert.assertEquals(1, configMetrics.getConfigNewContentsUpdates());
        Assert.assertEquals(0, configMetrics.getConfigSameContentsSkips());
    }

    @Test
    public void testMaximumSkips() {
        ConfigurationFetcher fetcher = (fileNames, application) -> {
            if ("myapp".equals(application) && ImmutableList.of("/a-featureflags.json", "/b-featureflags.json").equals(fileNames)) {
                return ImmutableMap.of("/a-featureflags.json", "{ \"feature-flags\": { \"enable-all\": { \"value\": true } } }",
                        "/b-featureflags.json", "{ \"feature-flags\": { \"enable-none\": { \"value\": false } } }");
            }
            return Collections.emptyMap();
        };

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        ScheduledExecutorService executorService = Mockito.mock(ScheduledExecutorService.class);
        ConfigurationLoaderMetrics configurationLoaderMetrics = new ConfigurationLoaderMetrics();

        Merci merci = new Merci(fetcher, executorService, objectMapper, digest);
        merci.setMaximumSkips(2);
        merci.setMetrics(configurationLoaderMetrics);

        FeatureFlagMetrics featureFlagMetrics = new FeatureFlagMetrics();
        FeatureFlagManager featureFlagManager = merci.addFeatureFlagManager("myapp").registerFile("/a-featureflags.json").registerFile("/b-featureflags.json")
                .setMetrics(featureFlagMetrics).build();

        ConfigurationLoader loader = merci.createLoader(Duration.ofSeconds(10));
        loader.start();

        Mockito.verify(executorService).scheduleWithFixedDelay(runnableCaptor.capture(), Matchers.anyLong(), Matchers.anyLong(), Matchers.any(TimeUnit.class));
        runnableCaptor.getValue().run();
        runnableCaptor.getValue().run();
        runnableCaptor.getValue().run();
        runnableCaptor.getValue().run();
        Assert.assertEquals(0, configurationLoaderMetrics.getConfigurationFailures());
        Assert.assertEquals(4, configurationLoaderMetrics.getConfigurationRequests());
        Assert.assertEquals(ImmutableList.of("enable-all", "enable-none"), featureFlagManager.getConfigurationNames());
        Assert.assertEquals(4, featureFlagMetrics.getFeatureFlagUpdates());
        Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagContentFailures());
        Assert.assertEquals(2, featureFlagMetrics.getFeatureFlagNewContentsUpdates());
        Assert.assertEquals(2, featureFlagMetrics.getFeatureFlagSameContentsSkips());
    }

    @Test
    public void testSkipNonInstantiableConfiguration() {
        ConfigurationFetcher fetcher = (fileNames, application) -> {
            if ("myapp".equals(application) && ImmutableList.of("/a-featureflags.json", "/b-featureflags.json").equals(fileNames)) {
                return ImmutableMap.of("/a-featureflags.json", "{ \"feature-flags\": { \"enable-all\": { \"value\": true } } }",
                        "/b-featureflags.json", "{ \"feature-flags\": { \"enable-none\": { \"value\": false } } }");
            } else if ("myapp".equals(application) && ImmutableList.of("/configs.json").equals(fileNames)) {
                return ImmutableMap.of("/configs.json", "{ \"configs\": { \"com.medallia.merci.core.configs.NumberConfig\": { \"value\" : { \"badproperty\" : 1 } } } }");
            }
            return Collections.emptyMap();
        };

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        ScheduledExecutorService executorService = Mockito.mock(ScheduledExecutorService.class);
        ConfigurationLoaderMetrics configurationLoaderMetrics = new ConfigurationLoaderMetrics();

        Merci merci = new Merci(fetcher, executorService, objectMapper, digest);
        merci.skipNonInstantiableConfiguration();
        merci.setMetrics(configurationLoaderMetrics);

        FeatureFlagMetrics featureFlagMetrics = new FeatureFlagMetrics();
        FeatureFlagManager featureFlagManager = merci.addFeatureFlagManager("myapp").registerFile("/a-featureflags.json").registerFile("/b-featureflags.json")
                .setMetrics(featureFlagMetrics).build();
        ConfigMetrics configMetrics = new ConfigMetrics();
        ConfigManager configManager = merci.addConfigManager("myapp").registerFile("/configs.json").setMetrics(configMetrics).build();

        ConfigurationLoader loader = merci.createLoader(Duration.ofSeconds(10));
        loader.start();

        Mockito.verify(executorService, Mockito.times(1)).scheduleWithFixedDelay(runnableCaptor.capture(), Matchers.anyLong(), Matchers.anyLong(), Matchers.any(TimeUnit.class));
        runnableCaptor.getAllValues().get(0).run();
        Assert.assertEquals(0, configurationLoaderMetrics.getConfigurationFailures());
        Assert.assertEquals(2, configurationLoaderMetrics.getConfigurationRequests());
        Assert.assertEquals(ImmutableList.of("enable-all", "enable-none"), featureFlagManager.getConfigurationNames());
        Assert.assertEquals(Collections.emptyList(), configManager.getConfigurationNames());
        Assert.assertEquals(2, featureFlagMetrics.getFeatureFlagUpdates());
        Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagContentFailures());
        Assert.assertEquals(1, featureFlagMetrics.getFeatureFlagNewContentsUpdates());
        Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagSameContentsSkips());
        Assert.assertEquals(0, configMetrics.getConfigUpdates());
        Assert.assertEquals(1, configMetrics.getConfigNonInstantiableSkips());
        Assert.assertEquals(0, configMetrics.getConfigContentFailures());
        Assert.assertEquals(1, configMetrics.getConfigNewContentsUpdates());
        Assert.assertEquals(0, configMetrics.getConfigSameContentsSkips());
    }

    @Test
    public void testFailNonInstantiableConfiguration() {
        ConfigurationFetcher fetcher = (fileNames, application) -> {
            if ("myapp".equals(application) && ImmutableList.of("/a-featureflags.json", "/b-featureflags.json").equals(fileNames)) {
                return ImmutableMap.of("/a-featureflags.json", "{ \"feature-flags\": { \"enable-all\": { \"value\": true } } }",
                        "/b-featureflags.json", "{ \"feature-flags\": { \"enable-none\": { \"value\": false } } }");
            } else if ("myapp".equals(application) && ImmutableList.of("/configs.json").equals(fileNames)) {
                return ImmutableMap.of("/configs.json", "{ \"configs\": { \"com.medallia.merci.core.configs.NumberConfig\": { \"value\" : { \"badproperty\" : 1 } } } }");
            }
            return Collections.emptyMap();
        };

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        ScheduledExecutorService executorService = Mockito.mock(ScheduledExecutorService.class);
        ConfigurationLoaderMetrics configurationLoaderMetrics = new ConfigurationLoaderMetrics();

        Merci merci = new Merci(fetcher, executorService, objectMapper, digest);
        merci.failNonInstantiableConfiguration();
        merci.setMetrics(configurationLoaderMetrics);

        FeatureFlagMetrics featureFlagMetrics = new FeatureFlagMetrics();
        FeatureFlagManager featureFlagManager = merci.addFeatureFlagManager("myapp").registerFile("/a-featureflags.json").registerFile("/b-featureflags.json")
                .setMetrics(featureFlagMetrics).build();
        ConfigMetrics configMetrics = new ConfigMetrics();
        ConfigManager configManager = merci.addConfigManager("myapp").registerFile("/configs.json").setMetrics(configMetrics).build();

        merci.createAndStartLoader(Duration.ofSeconds(10));

        Mockito.verify(executorService, Mockito.times(1)).scheduleWithFixedDelay(runnableCaptor.capture(), Matchers.anyLong(), Matchers.anyLong(), Matchers.any(TimeUnit.class));
        runnableCaptor.getAllValues().get(0).run();
        Assert.assertEquals(0, configurationLoaderMetrics.getConfigurationFailures());
        Assert.assertEquals(2, configurationLoaderMetrics.getConfigurationRequests());
        Assert.assertEquals(ImmutableList.of("enable-all", "enable-none"), featureFlagManager.getConfigurationNames());
        Assert.assertEquals(Collections.emptyList(), configManager.getConfigurationNames());
        Assert.assertEquals(2, featureFlagMetrics.getFeatureFlagUpdates());
        Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagContentFailures());
        Assert.assertEquals(1, featureFlagMetrics.getFeatureFlagNewContentsUpdates());
        Assert.assertEquals(0, featureFlagMetrics.getFeatureFlagSameContentsSkips());
        Assert.assertEquals(0, configMetrics.getConfigUpdates());
        Assert.assertEquals(0, configMetrics.getConfigNonInstantiableSkips());
        Assert.assertEquals(1, configMetrics.getConfigContentFailures());
        Assert.assertEquals(1, configMetrics.getConfigNewContentsUpdates());
        Assert.assertEquals(0, configMetrics.getConfigSameContentsSkips());
    }

    @Test
    public void testFetcherThrowsException() {
        ConfigurationFetcher fetcher = (fileNames, application) -> {
            if ("myapp".equals(application) && ImmutableList.of("/a-featureflags.json", "/b-featureflags.json").equals(fileNames)) {
                return ImmutableMap.of("/a-featureflags.json", "{ \"feature-flags\": { \"enable-all\": { \"value\": true } } }",
                        "/b-featureflags.json", "{ \"feature-flags\": { \"enable-none\": { \"value\": false } } }");
            } else if ("myapp".equals(application) && ImmutableList.of("/configs.json").equals(fileNames)) {
                throw new IOException("problems");
            }
            return Collections.emptyMap();
        };

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        ScheduledExecutorService executorService = Mockito.mock(ScheduledExecutorService.class);
        ConfigurationLoaderMetrics configurationLoaderMetrics = new ConfigurationLoaderMetrics();

        Merci merci = new Merci(fetcher, executorService, objectMapper, digest);
        merci.setMetrics(configurationLoaderMetrics);

        FeatureFlagManager featureFlagManager = merci.addFeatureFlagManager("myapp").registerFile("/a-featureflags.json").registerFile("/b-featureflags.json").build();
        ConfigManager configManager = merci.addConfigManager("myapp").registerFile("/configs.json").build();

        ConfigurationLoader loader = merci.createLoader(Duration.ofSeconds(10));
        loader.start();

        Mockito.verify(executorService, Mockito.times(1)).scheduleWithFixedDelay(runnableCaptor.capture(), Matchers.anyLong(), Matchers.anyLong(), Matchers.any(TimeUnit.class));
        runnableCaptor.getAllValues().get(0).run();
        Assert.assertEquals(1, configurationLoaderMetrics.getConfigurationFailures());
        Assert.assertEquals(2, configurationLoaderMetrics.getConfigurationRequests());
        Assert.assertEquals(ImmutableList.of("enable-all", "enable-none"), featureFlagManager.getConfigurationNames());
        Assert.assertEquals(Collections.emptyList(), configManager.getConfigurationNames());
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException(exception);
        }
    }
}