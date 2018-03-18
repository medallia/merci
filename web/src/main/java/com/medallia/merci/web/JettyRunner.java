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
package com.medallia.merci.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medallia.merci.core.ConfigurationLoader;
import com.medallia.merci.core.FeatureFlagManager;
import com.medallia.merci.core.JsonConfigManager;
import com.medallia.merci.core.Merci;
import com.medallia.merci.core.filesystem.ConfigurationFetcherMetrics;
import com.medallia.merci.core.fetcher.ConfigurationFetcher;
import com.medallia.merci.core.filesystem.FilesystemConfigurationFetcher;
import com.medallia.merci.web.environment.Environment;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * Example of Jetty based application for 'Merci as a Service'.
 */
public class JettyRunner {

    /**
     * Main method of the application, initializes Merci runner and starts it.
     *
     * @param args command-line arguments
     * @throws Exception thrown during start of Jetty server
     */
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public static void main(String[] args) throws Exception {
        Environment environment = new Environment("local", System.getenv(), System.getProperties());
        URI resource = Thread.currentThread().getContextClassLoader() .getResource("configurations").toURI();
        String path = Paths.get(resource).toAbsolutePath().toString();
        ConfigurationFetcher fetcher = new FilesystemConfigurationFetcher(FileSystems.getDefault(), path,true, new ConfigurationFetcherMetrics());

        Merci merci = new Merci(fetcher);
        merci.setMaximumSkips(0);
        merci.skipNonInstantiableConfiguration();

        FeatureFlagManager featureFlagManager = merci.addFeatureFlagManager("medallia-merci")
                .registerFile("/featureflags.json").build();
        JsonConfigManager jsonConfigManager = merci.addJsonConfigManager("medallia-merci")
                .registerFile("/configs.json").build();
        ConfigurationLoader loader = merci.createLoader(Duration.ofSeconds(10));

        MerciRunner runner = new MerciRunner(environment, new ObjectMapper(), loader, featureFlagManager, jsonConfigManager);
        runner.start();
    }
}
