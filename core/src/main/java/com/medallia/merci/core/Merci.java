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
import com.medallia.merci.core.fetcher.ConfigurationFetcher;
import com.medallia.merci.core.metrics.ConfigMetrics;
import com.medallia.merci.core.metrics.ConfigurationLoaderMetrics;
import com.medallia.merci.core.metrics.FeatureFlagMetrics;

import com.medallia.merci.core.metrics.JsonConfigMetrics;
import com.medallia.merci.core.utils.ClassFinder;
import com.medallia.merci.core.utils.DefaultClassFinder;

import java.security.MessageDigest;

import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Main Merci initializer, that creates {@link FeatureFlagManager}, {@link ConfigManager}, {@link JsonConfigManager}
 * and {@link ConfigurationLoader} instances.
 */
public final class Merci {

    private final ConfigurationFetcher fetcher;
    private final ScheduledExecutorService executorService;
    private final ObjectMapper objectMapper;
    private final MessageDigest digest;
    private final List<ConfigurationReader> readers;
    private ConfigurationLoaderMetrics metrics;
    private boolean skipNonInstantiable;
    private int maximumSkips;

    /**
     * Creates main Merci initializer with provided configuration fetcher.
     *
     * @param fetcher configuration fetcher
     */
    public Merci(ConfigurationFetcher fetcher) {
        this(fetcher, Executors.newScheduledThreadPool(3), new ObjectMapper(), sha256());
    }

    /**
     * Creates main Merci initializer with provided configuration fetcher, executor service, object mapper and message digest.
     *
     * @param fetcher configuration fetcher
     * @param executorService executor service for scheduling asynchronous configuration tasks
     * @param objectMapper Jackson JSON deserializer
     * @param digest message digest
     */
    public Merci(ConfigurationFetcher fetcher, ScheduledExecutorService executorService, ObjectMapper objectMapper, MessageDigest digest) {
        this.fetcher = fetcher;
        this.executorService = executorService;
        this.objectMapper = objectMapper;
        this.digest = digest;
        readers = new ArrayList<>();
        skipNonInstantiable = true;
        maximumSkips = 0;
    }

    /**
     * Sets metrics for loader.
     *
     * @param metrics metrics for loader
     */
    public void setMetrics(ConfigurationLoaderMetrics metrics) {
        this.metrics = metrics;
    }

    /**
     * Sets maximum number of skips, until a refresh is forced.
     *
     * @param maximumSkips
     */
    public void setMaximumSkips(int maximumSkips) {
        this.maximumSkips = maximumSkips;
    }

    /**
     * Continues loading configurations, just skip each non-instantiable configuration.
     */
    public void skipNonInstantiableConfiguration() {
        skipNonInstantiable = true;
    }

    /**
     * Stops loading of all configurations in case of a non-instantiable configuration.
     */
    public void failNonInstantiableConfiguration() {
        skipNonInstantiable = false;
    }

    /**
     * Creates builder with new {@link FeatureFlagManager} for provided application.
     *
     * @param application name of application
     * @return new builder for feature flag manager
     */
    public FeatureFlagManagerBuilder addFeatureFlagManager(String application) {
        return new FeatureFlagManagerBuilder(application);
    }

    /**
     * Creates builder with new {@link ConfigManager} for provided application.
     *
     * @param application name of application
     * @return new builder for config manager
     */
    public ConfigManagerBuilder addConfigManager(String application) {
        return new ConfigManagerBuilder(application);
    }

    /**
     * Creates builder with new {@link JsonConfigManager} for provided application.
     *
     * @param application name of application
     * @return new builder for Json config manager
     */
    public JsonConfigManagerBuilder addJsonConfigManager(String application) {
        return new JsonConfigManagerBuilder(application);
    }

    /**
     * Creates new {@link ConfigurationLoader} with provided refresh interval and immediately start it.
     *
     * @param refreshInterval interval between cycles of loading configuration
     * @return new, started configuration loader
     */
    public ConfigurationLoader createAndStartLoader(Duration refreshInterval) {
        ConfigurationLoader loader = createLoader(refreshInterval);
        loader.start();
        return loader;
    }

    /**
     * Creates new {@link ConfigurationLoader} with provided refresh interval.
     *
     * @param refreshInterval interval between cycles of loading configuration
     * @return new configuration loader
     */
    public ConfigurationLoader createLoader(Duration refreshInterval) {
        if (metrics == null) {
            metrics = new ConfigurationLoaderMetrics();
        }
        ConfigurationLoader loader = new ConfigurationLoader(metrics, Collections.unmodifiableList(new ArrayList<>(readers)),
                executorService, refreshInterval);
        readers.clear();
        return loader;
    }

    /**
     * Builder for {@link FeatureFlagManager}.
     */
    public class FeatureFlagManagerBuilder {

        private final String application;
        private final List<String> fileNames;
        private final String rootNode;
        private FeatureFlagMetrics metrics;

        /**
         * Creates builder for {@link FeatureFlagManager}.
         */
        public FeatureFlagManagerBuilder(String application) {
            this.application = application;
            this.fileNames = new ArrayList<>();
            rootNode = "feature-flags";
        }

        /**
         * Register name of file with feature flags.
         */
        public FeatureFlagManagerBuilder registerFile(String fileName) {
            this.fileNames.add(fileName);
            return this;
        }

        /**
         * Set metrics.
         */
        public FeatureFlagManagerBuilder setMetrics(FeatureFlagMetrics metrics) {
            this.metrics = metrics;
            return this;
        }

        /**
         * @return {@link FeatureFlagManager}.
         */
        public FeatureFlagManager build() {
            FeatureFlagManager featureFlagManager = new FeatureFlagManager();
            if (metrics == null) {
                metrics = new FeatureFlagMetrics();
            }
            FeatureFlagMapper featureFlagMapper = new FeatureFlagMapper(rootNode, skipNonInstantiable, objectMapper, metrics);
            ConfigurationReader<Boolean> featureFlagReader = new ConfigurationReader<>(application, fileNames, fetcher, featureFlagMapper, featureFlagManager, digest, metrics, maximumSkips);
            readers.add(featureFlagReader);
            return featureFlagManager;
        }
    }

    /**
     * Builder for {@link ConfigManager}.
     */
    public class ConfigManagerBuilder {

        private final String application;
        private final List<String> fileNames;
        private final String rootNode;
        private final ClassFinder<Object> classFinder;
        private ConfigMetrics metrics;

        /**
         * Creates builder for {@link ConfigManager}.
         */
        public ConfigManagerBuilder(String application) {
            this.application = application;
            this.fileNames = new ArrayList<>();
            rootNode = "configs";
            classFinder = new DefaultClassFinder();
        }

        /**
         * Register name of file with configs.
         */
        public ConfigManagerBuilder registerFile(String fileName) {
            this.fileNames.add(fileName);
            return this;
        }

        /**
         * Set metrics.
         */
        public ConfigManagerBuilder setMetrics(ConfigMetrics metrics) {
            this.metrics = metrics;
            return this;
        }

        /**
         * @return {@link ConfigManager}.
         */
        public ConfigManager build() {
            ConfigManager configManager = new ConfigManager();
            if (metrics == null) {
                metrics = new ConfigMetrics();
            }
            ConfigurationMapper<Object> configMapper = new ConfigurationMapper<>(rootNode, skipNonInstantiable, objectMapper, metrics, classFinder);
            ConfigurationReader<Object> configReader = new ConfigurationReader<>(application, fileNames, fetcher, configMapper, configManager, digest, metrics, maximumSkips);
            readers.add(configReader);
            return configManager;
        }
    }

    /**
     * Builder for {@link JsonConfigManager}.
     */
    public class JsonConfigManagerBuilder {

        private final String application;
        private final List<String> fileNames;
        private final String rootNode;
        private JsonConfigMetrics metrics;

        /**
         * Creates builder for {@link JsonConfigManager}.
         */
        public JsonConfigManagerBuilder(String application) {
            this.application = application;
            this.fileNames = new ArrayList<>();
            rootNode = "configs";
        }

        /**
         * Register name of file with Json configs.
         */
        public JsonConfigManagerBuilder registerFile(String fileName) {
            this.fileNames.add(fileName);
            return this;
        }

        /**
         * Set metrics.
         */
        public JsonConfigManagerBuilder setMetrics(JsonConfigMetrics metrics) {
            this.metrics = metrics;
            return this;
        }

        /**
         * @return {@link JsonConfigManager}.
         */
        public JsonConfigManager build() {
            JsonConfigManager configManager = new JsonConfigManager();
            if (metrics == null) {
                metrics = new JsonConfigMetrics();
            }
            JsonConfigMapper configMapper = new JsonConfigMapper(rootNode, skipNonInstantiable, objectMapper, metrics);
            ConfigurationReader<JsonNode> configReader = new ConfigurationReader<>(application, fileNames, fetcher, configMapper, configManager, digest, metrics, maximumSkips);
            readers.add(configReader);
            return configManager;
        }
    }

    /**
     * @return SHA-256 message digest instance.
     * @throws IllegalStateException in case lookup throws NoSuchAlgorithmException
     */
    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
