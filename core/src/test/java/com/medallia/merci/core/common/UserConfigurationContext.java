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
 * User-specific configuration context, pre-populated with context-key and -value mappings of environment, cluster and user.
 */
public final class UserConfigurationContext extends ConfigurationContext {

    public static final String ENVIRONMENT = "environment";
    public static final String CLUSTER = "cluster";
    public static final String USER = "user";

    /**
     * Constructs configuration context based on provided environment, cluster and user values.
     *
     * @param environment environment
     * @param cluster cluster
     * @param user user
     */
    public UserConfigurationContext(String environment, String cluster, String user) {
        this.put(ENVIRONMENT, environment);
        this.put(CLUSTER, cluster);
        this.put(USER, user);
    }
}
