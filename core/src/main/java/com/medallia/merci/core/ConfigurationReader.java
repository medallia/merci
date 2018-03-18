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

import com.medallia.merci.core.fetcher.ConfigurationFetcher;
import com.medallia.merci.core.metrics.UpdateConfigurationMetrics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reader for configurations.
 *
 *  @param <T> type of configuration
 */
public class ConfigurationReader<T> {

    private final String application;
    private final List<String> fileNames;
    private final ConfigurationFetcher fetcher;
    private final ConfigurationMapper<T> parser;
    private final ConfigurationManager<T> manager;

    /** Message digest. */
    private final MessageDigest digest;

    /** Metrics for configurations. */
    private final UpdateConfigurationMetrics metrics;

    /** Maximum number of time the injected (decorated) configuration adapter will not be updated in case of same content. */
    private final int maximumSkips;

    /** Number of same-content skips left before updating the injected (decorated) configuration adapter. */
    private final AtomicInteger skipsLeft;

    /** Hash of configuration content from response of previous config request. */
    private byte[] previousHash;

    /**
     * Creates configuration read.
     *
     * @param application application
     * @param fileNames names of textual configuration files
     * @param fetcher fetcher for remote or local configuration files
     * @param parser deserializer for textual configuration files
     * @param manager configuration manager
     * @param digest message digest
     * @param metrics metrics
     * @param maximumSkips maximum number of skips
     */
    public ConfigurationReader(String application, List<String> fileNames,
                               ConfigurationFetcher fetcher,
                               ConfigurationMapper<T> parser,
                               ConfigurationManager<T> manager,
                               MessageDigest digest,
                               UpdateConfigurationMetrics metrics,
                               final int maximumSkips) {
        this.application = application;
        this.fetcher = fetcher;
        this.parser = parser;
        this.manager = manager;
        this.fileNames = fileNames;
        this.digest = digest;
        this.metrics = metrics;
        previousHash = new byte[0];
        this.maximumSkips = maximumSkips;
        skipsLeft = new AtomicInteger(maximumSkips);
    }

    /**
     * Execute fetch, parse and store of configurations.
     *
     * @throws IOException in case of a failure
     */
    public void execute() throws IOException {
        Map<String, String> contents = fetcher.fetch(fileNames, application);
        contents.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(entry -> digest.update(entry.getValue().getBytes(StandardCharsets.UTF_8)));
        byte[] hash = digest.digest();
        if (skipsLeft.getAndDecrement() > 0 && Arrays.equals(previousHash, hash)) {
            metrics.incrementSameContentsSkips();
        } else {
            metrics.incrementNewContentsUpdates();
            updateConfigurationManager(contents);
            previousHash = hash;
            skipsLeft.set(maximumSkips);
        }
    }

    private void updateConfigurationManager(Map<String, String> contents) {
        int numConfigurations = 0;
        int numContentFailures = 0;
        Map<String, Configuration<T>> configurationCache = new LinkedHashMap<> ();
        for (String content : contents.values()) {
            try {
                Map<String, Configuration<T>> configurations = parser.readValue(content);
                numConfigurations += configurations.size();
                configurationCache.putAll(configurations);
            } catch (IOException exception) {
                numContentFailures++;
            }
        }
        metrics.incrementContentFailures(numContentFailures);
        metrics.incrementNameDuplicates(numConfigurations - configurationCache.size());
        metrics.incrementUpdates(configurationCache.size());
        manager.updateConfigurations(configurationCache);
    }

    /**
     * Sets internal counter of possible number of skips to zero (reset).
     */
    public void reset() {
        skipsLeft.lazySet(0);
    }
}
