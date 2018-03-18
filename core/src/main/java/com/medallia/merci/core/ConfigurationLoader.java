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

import com.medallia.merci.core.metrics.ConfigurationLoaderMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Configuration loader, that uses an executor service to asynchronously fetch, parse and store in-memory configurations.
 *
 * @NotThreadSafe
 */
public final class ConfigurationLoader {

    private static final Duration INITIAL_DELAY = Duration.ofSeconds(0);
    private static final Duration AWAIT_TERMINATION = Duration.ofSeconds(3);

    private final Logger log = LoggerFactory.getLogger(ConfigurationLoader.class);
    private final List<ConfigurationReader> configurationReaders;
    private final ScheduledExecutorService executorService;
    private final ConfigurationLoaderMetrics metrics;
    private final Duration refreshInterval;


    /*
     * Constructs Configuration loader based on provided configs properties, HTTP client, executor service.
     *
     * @param configurationUpdates list of configuration updates
     * @param executorService executor service

     */

    /**
     * Constructs Configuration loader based on provided metrics, configuration read (tasks) and an executor service.
     *
     * @param metrics metrics for the loader
     * @param configurationReaders list of reader tasks
     * @param executorService service to periodically fetch, parse and store configurations
     * @param refreshInterval time in seconds between scheduled read tasks
     */
    public ConfigurationLoader(ConfigurationLoaderMetrics metrics,
                               List<ConfigurationReader> configurationReaders,
                               ScheduledExecutorService executorService,
                               Duration refreshInterval) {
        this.metrics = metrics;
        this.configurationReaders = configurationReaders;
        this.executorService = executorService;
        this.refreshInterval = refreshInterval;
    }

    /**
     * Start periodic refresh of configurations by scheduling executor task to call execute method on fixed schedule basis.
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void start() {
        for(ConfigurationReader configurationReader: configurationReaders) {
            executorService.scheduleWithFixedDelay(() -> {
                try {
                    metrics.incrementConfigurationRequests();
                    configurationReader.execute();
                } catch (RuntimeException exception) {
                    metrics.incrementConfigurationFailures();
                    log.error("Skipped updating configurations due to exception ", exception);
                    throw exception;
                } catch (IOException exception) {
                    metrics.incrementConfigurationFailures();
                    log.error("Skipped updating configurations due to exception ", exception);
                }
            }, INITIAL_DELAY.getSeconds(), refreshInterval.getSeconds(), TimeUnit.SECONDS);
        }
    }

    /**
     * Stop scheduled periodic refresh of configurations.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(AWAIT_TERMINATION.getSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
