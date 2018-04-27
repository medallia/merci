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
import com.google.common.collect.ImmutableMap;
import com.medallia.merci.core.fetcher.ConfigurationFetcher;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ConfigurationLoader}.
 */
public class ConfigurationLoaderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final MessageDigest digest = createMessageDigest();

    private final FeatureFlagManager featureFlagManager = new FeatureFlagManager();

    private final ConfigurationLoaderMetrics configLoaderMetrics = new ConfigurationLoaderMetrics();
    private final FeatureFlagMetrics featureFlagMetrics = new FeatureFlagMetrics();
    private final ConfigurationMapper<Boolean> featureFlagMapper = new FeatureFlagMapper("feature-flags", true, objectMapper, featureFlagMetrics);

    /**
     * Tests that Configuration Fetcher throwing an IOException results in a configuration failure.
     */
    @Test
    public void testConfigurationFetcherThrowingIOExceptionResultsInConfigurationFailure() {
        ConfigurationFetcher configurationFetcher = (fileNames, application) -> { throw new IOException("problems"); };

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        ScheduledExecutorService executorService = Mockito.mock(ScheduledExecutorService.class);

        ConfigurationReader<Boolean> featureFlagReader = new ConfigurationReader<>("myapp-configurations", Arrays.asList("/featureflags.json"),
                configurationFetcher, featureFlagMapper, featureFlagManager, digest, featureFlagMetrics, 0);

        ConfigurationLoader configurationLoader = new ConfigurationLoader(configLoaderMetrics, Arrays.asList(featureFlagReader), executorService, Duration.ofSeconds(1));
        configurationLoader.start();
        Mockito.verify(executorService).scheduleWithFixedDelay(runnableCaptor.capture(), Matchers.anyLong(), Matchers.anyLong(), Matchers.any(TimeUnit.class));
        runnableCaptor.getValue().run();
        Assert.assertEquals(1, configLoaderMetrics.getConfigurationFailures());
        Assert.assertEquals(1, configLoaderMetrics.getConfigurationRequests());
    }

    /**
     * Tests that Configuration Fetcher throwing a RuntimeException results in a configuration failure.
     */
    @Test(expected = RuntimeException.class)
    public void testConfigurationFetcherThrowingRuntimeExceptionResultsInConfigurationFailure()  {
        ConfigurationFetcher configurationFetcher = (fileNames, application) -> { throw new RuntimeException("problems"); };

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        ScheduledExecutorService executorService = Mockito.mock(ScheduledExecutorService.class);

        ConfigurationReader<Boolean> featureFlagReader = new ConfigurationReader<>("myapp-configurations", Arrays.asList("/featureflags.json"),
                configurationFetcher, featureFlagMapper, featureFlagManager, digest, featureFlagMetrics, 0);

        ConfigurationLoader configurationLoader = new ConfigurationLoader(configLoaderMetrics, Arrays.asList(featureFlagReader), executorService, Duration.ofSeconds(1));
        configurationLoader.start();
        Mockito.verify(executorService).scheduleWithFixedDelay(runnableCaptor.capture(), Matchers.anyLong(), Matchers.anyLong(), Matchers.any(TimeUnit.class));
        runnableCaptor.getValue().run();
        Assert.assertEquals(1, configLoaderMetrics.getConfigurationFailures());
        Assert.assertEquals(1, configLoaderMetrics.getConfigurationRequests());
    }

    /**
     * Tests that Configuration Loader Shutdown Shuts down ExecutorService.
     */
    @Test
    public void testConfigurationLoaderShutdownShutsdownExecutorService() throws InterruptedException, NoSuchAlgorithmException {
        ConfigurationFetcher configurationFetcher = (fileNames, application)
                -> ImmutableMap.of("com/medallia/merci/core/configs/featureflags.json", "{ \"feature-flags\": { \"enable-all\": { \"value\": true } } }");

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        ScheduledExecutorService executorService = Mockito.mock(ScheduledExecutorService.class);

        ConfigurationReader<Boolean> featureFlagReader = new ConfigurationReader<>("myapp-configurations", Arrays.asList("/featureflags.json"),
                configurationFetcher, featureFlagMapper, featureFlagManager, digest, featureFlagMetrics, 0);

        ConfigurationLoader configLoader = new ConfigurationLoader(configLoaderMetrics, Arrays.asList(featureFlagReader), executorService,Duration.ofSeconds(1));
        configLoader.start();
        Mockito.verify(executorService, Mockito.times(1)).scheduleWithFixedDelay(runnableCaptor.capture(), Matchers.anyLong(), Matchers.anyLong(), Matchers.any(TimeUnit.class));
        runnableCaptor.getValue().run();
        configLoader.shutdown();
        Mockito.verify(executorService, Mockito.times(1)).shutdown();
        Mockito.verify(executorService, Mockito.times(1)).awaitTermination(Matchers.anyLong(), Matchers.any(TimeUnit.class));
    }

    /**
     * Tests that Configuration Loader Shutdown Rethrows Exception When Executor Service Throws InterruptedException.
     */
    @Test
    public void testConfigurationLoaderShutdownRethrowsExceptionWhenExecutorServiceThrowsInterruptedException() throws InterruptedException, NoSuchAlgorithmException {
        ConfigurationFetcher configurationFetcher = (fileNames, application)
                -> ImmutableMap.of("com/medallia/merci/core/configs/featureflags.json", "{ \"feature-flags\": { \"enable-one\": { \"value\": true } } }");

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        ScheduledExecutorService executorService = Mockito.mock(ScheduledExecutorService.class);

        ConfigurationMapper<Boolean> configurationMapper = new FeatureFlagMapper("feature-flags", true, objectMapper, featureFlagMetrics);
        ConfigurationReader<Boolean> featureFlagUpdate = new ConfigurationReader<>("myapp-configurations", Arrays.asList("/featureflags.json"),
                configurationFetcher, configurationMapper, featureFlagManager, digest, featureFlagMetrics, 0);

        ConfigurationLoader configLoader = new ConfigurationLoader(configLoaderMetrics, Arrays.asList(featureFlagUpdate), executorService, Duration.ofSeconds(1));
        configLoader.start();
        Mockito.verify(executorService, Mockito.times(1)).scheduleWithFixedDelay(runnableCaptor.capture(), Matchers.anyLong(), Matchers.anyLong(), Matchers.any(TimeUnit.class));
        runnableCaptor.getValue().run();
        Mockito.doThrow(InterruptedException.class).when(executorService).awaitTermination(Matchers.anyLong(), Matchers.any(TimeUnit.class));
        configLoader.shutdown();
    }

    /**
     * Tests the {@link ConfigurationLoader#waitUntilInitialConfigLoadComplete()} method works as expected.
     */
    @Test
    public void testWaitUntilInitialConfigLoadComplete() throws InterruptedException {
        // Setup config loader to load some dummy config
        final ConfigurationFetcher configurationFetcher = (fileNames, application) -> ImmutableMap.of(
                "com/medallia/merci/core/configs/featureflags.json",
                "{ \"feature-flags\": { \"enable-all\": { \"value\": true } } }"
        );
        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(0);
        final ConfigurationReader<Boolean> featureFlagReader = new ConfigurationReader<>(
                "myapp-configurations", Collections.singletonList("/featureflags.json"),
                configurationFetcher, featureFlagMapper, featureFlagManager, digest, featureFlagMetrics,
                0);
        final ConfigurationLoader configLoader = new ConfigurationLoader(
                configLoaderMetrics, Collections.singletonList(featureFlagReader),
                executorService, Duration.ofSeconds(1));

        // Before starting config loader, the "initial config load complete" flag is false
        assertFalse(configLoader.isInitialConfigLoadComplete());

        // Start the config loader
        configLoader.start();

        // Wait for initial config load to complete
        configLoader.waitUntilInitialConfigLoadComplete();

        // Now the wait is up, the "initial config load complete" flag will be true
        assertTrue(configLoader.isInitialConfigLoadComplete());

        // Clean up after ourselves: shut down the config loader
        configLoader.shutdown();

        // Clean up after ourselves: shut down the executorService
        executorService.shutdown();
    }

    private static MessageDigest createMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException(exception);
        }
    }
}
