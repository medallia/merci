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

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Jersey application (resource) config for feature flags and configs.
 */
public class ConfigurationResourceConfig extends ResourceConfig {

    /**
     * Creates a Jersey application (configuration) and registers all of its controllers for the REST endpoints, as well
     * as their factory bindings.
     *
     * @param jacksonJsonProvider Jackson Json Provider
     * @param controllerBinder factory bindings for all controllers of the application
     * @param controllerClasses classes of controllers, that need to be registered in this application
     */
    public ConfigurationResourceConfig(JacksonJsonProvider jacksonJsonProvider,
                                       AbstractBinder controllerBinder,
                                       Class<?>... controllerClasses) {

        registerInstances(jacksonJsonProvider);
        for (Class<?> controllerClass : controllerClasses) {
            register(controllerClass);
        }
        register(controllerBinder);
    }
}
