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
package com.medallia.merci.core.common;

import com.medallia.merci.core.ConfigurationContext;

/**
 * Environment-specific configuration context, pre-populated with context-key and -value mappings of environment.
 */
public final class EnvironmentConfigurationContext extends ConfigurationContext {

    public static final String ENVIRONMENT = "environment";

    /**
     * Constructs configuration context based on provided environment value.
     *
     * @param environment environment
     */
    public EnvironmentConfigurationContext(String environment) {
        this.put(ENVIRONMENT, environment);
    }
}
