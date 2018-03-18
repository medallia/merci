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
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import com.medallia.merci.web.configs.ConfigController;
import com.medallia.merci.web.configs.ConfigControllerFactory;
import com.medallia.merci.core.ConfigurationLoader;
import com.medallia.merci.web.environment.Environment;
import com.medallia.merci.web.exception.ApiExceptionMapper;
import com.medallia.merci.web.featureflags.FeatureFlagController;
import com.medallia.merci.web.featureflags.FeatureFlagControllerFactory;
import com.medallia.merci.core.FeatureFlagManager;
import com.medallia.merci.core.JsonConfigManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.io.File;
import java.time.Duration;

/**
 * Runner, that uses Jetty to run a  Feature Flags and Configs web application.
 */
public class MerciRunner {

    private static final int DEFAULT_PORT = 8080;
    private static final Duration DEFAULT_IDLE_TIMEOUT = Duration.ofMillis(300000);
    private static final String JAVA_IO_TMPDIR_NAME = "java.io.tmpdir";

    private final Server server;
    private final ObjectMapper objectMapper;
    private final Environment environment;
    private final ConfigurationLoader configurationLoader;

    /**
     * Creates Merci Runner based on provided dependencies.
     *
     * @param environment environment
     * @param objectMapper JSON de-serializer for JSON and YAML
     * @param configurationLoader configuration loader
     * @param featureFlagManager feature flag manager
     * @param jsonConfigManager JsonNode config manager
     */
    public MerciRunner(Environment environment,
                       ObjectMapper objectMapper,
                       ConfigurationLoader configurationLoader,
                       FeatureFlagManager featureFlagManager,
                       JsonConfigManager jsonConfigManager) {
        this.objectMapper = objectMapper;
        this.environment = environment;
        this.configurationLoader = configurationLoader;
        server = createServer(featureFlagManager, jsonConfigManager);
    }

    /**
     *
     * @param objectMapper JSON and YAML de-serializer
     * @return new application configuration
     */

    /**
     * Creates new application (resource) configuration based on provided featureFlagManager, JsonConfigManager and Jackson JSON deserializer.
     *
     * @param featureFlagManager manager (store) for feature flags
     * @param jsonConfigManager manager (store) for JSON configs
     * @param objectMapper JSON and YAML de-serializer
     * @return new ResourceConfig
     */
    private ResourceConfig createApplicationConfiguration(FeatureFlagManager featureFlagManager, JsonConfigManager jsonConfigManager,
                                                          ObjectMapper objectMapper) {
        JacksonJaxbJsonProvider jacksonJsonProvider = new JacksonJaxbJsonProvider();
        jacksonJsonProvider.setMapper(objectMapper);
        final AbstractBinder binder = new ControllerFactoryBinder(
                new FeatureFlagControllerFactory(featureFlagManager),
                new ConfigControllerFactory(jsonConfigManager));
        return new ConfigurationResourceConfig(jacksonJsonProvider, binder,
                FeatureFlagController.class, ConfigController.class, ApiExceptionMapper.class
        );
    }

    @SuppressWarnings("PMD.LongVariable")
    private static final class ControllerFactoryBinder extends AbstractBinder {

        private final FeatureFlagControllerFactory featureFlagControllerFactory;
        private final ConfigControllerFactory configControllerFactory;

        public ControllerFactoryBinder(FeatureFlagControllerFactory featureFlagControllerFactory,
                                       ConfigControllerFactory configControllerFactory) {
            this.featureFlagControllerFactory = featureFlagControllerFactory;
            this.configControllerFactory = configControllerFactory;
        }

        @Override
        protected void configure() {
            bindFactory(QueryParametersFactory.class).to(QueryParameters.class).in(RequestScoped.class);
            bindFactory(featureFlagControllerFactory).to(FeatureFlagController.class).in(RequestScoped.class);
            bindFactory(configControllerFactory).to(ConfigController.class).in(RequestScoped.class);
        }
    }

    /**
     * Creates new Jetty server and adds a
     *
     * @param featureFlagManager manager (store) for feature flags
     * @param configManager manager (store) for JSON configs
     * @return new Jetty server
     */
    private Server createServer(FeatureFlagManager featureFlagManager, JsonConfigManager configManager) {
        Server jettyServer = new Server();
        ServerConnector serverConnector = new ServerConnector(jettyServer);
        serverConnector.setPort(DEFAULT_PORT);
        serverConnector.setIdleTimeout(DEFAULT_IDLE_TIMEOUT.toMillis());
        jettyServer.addConnector(serverConnector);

        ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContext.setAttribute("javax.servlet.context.tempdir", new File("../tmp/mememe"));

        WebAppContext appContext = new WebAppContext(null, null, null, null, null, null, ServletContextHandler.SESSIONS);
        appContext.setPersistTempDirectory(true);

        servletContext.setContextPath("/");
        servletContext.setResourceBase(environment.getStringProperty(JAVA_IO_TMPDIR_NAME, ""));
        ResourceConfig application = createApplicationConfiguration(featureFlagManager, configManager, objectMapper);
        ServletContainer servletContainer = new ServletContainer(application);

        servletContext.addServlet(new ServletHolder(servletContainer), "/*");

        jettyServer.setHandler(servletContext);
        jettyServer.setStopAtShutdown(true);
        return jettyServer;
    }

    /**
     * Starts Jetty server after initializing and starting configuration loader.
     *
     * @throws Exception in case of initialization or start error
     */
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void start() throws Exception {
        configurationLoader.start();
        //start Jetty HTTP Servlet container.
        server.start();
        server.join();
    }
}
