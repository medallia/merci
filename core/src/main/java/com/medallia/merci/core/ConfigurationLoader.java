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
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final AtomicBoolean initialConfigLoadComplete = new AtomicBoolean();

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
     * Has the initial configuration load been completed? The initial configuration load is considered to be
     * completed when an attempt has been made to read from each of the configured config readers. Note that the
     * initial configuration load is marked as complete even if one or more of those configured config readers failed
     * to load. The purpose of this flag is to prevent race conditions at startup (when we attempt to act on the
     * config before it is first loaded), not to deal with scenarios when the config load fails (e.g. due to
     * intermittent downtime in some remote server from which we are downloading the config.)
     *
     * @return true if initial configuration load been completed
     */
    public boolean isInitialConfigLoadComplete() {
        return initialConfigLoadComplete.get();
    }

    /**
     * Causes this thread to wait until the initial configuration load has been completed. Be careful that the
     * thread from which you call this method on has no role to play in the configuration loading process, otherwise
     * a deadlock may result.
     *
     * @throws InterruptedException if this thread is interrupted while waiting.
     */
    public void waitUntilInitialConfigLoadComplete() throws InterruptedException {
        synchronized (initialConfigLoadComplete) {
            while (true) {
                if (initialConfigLoadComplete.get())
                    return;
                initialConfigLoadComplete.wait();
            }
        }
    }

    /**
     * Mark the initial config load as having completed. Also notifies any threads which are waiting on its completion.
     */
    private void setInitialConfigLoadComplete() {
        synchronized (initialConfigLoadComplete) {
            initialConfigLoadComplete.set(true);
            initialConfigLoadComplete.notifyAll();
        }
    }

    /**
     * Start periodic refresh of configurations by scheduling executor task to call execute method on fixed schedule basis.
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void start() {
        executorService.scheduleWithFixedDelay(() -> {
            for(ConfigurationReader configurationReader: configurationReaders) {
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
            }
            setInitialConfigLoadComplete();
        }, INITIAL_DELAY.getSeconds(), refreshInterval.getSeconds(), TimeUnit.SECONDS);
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
