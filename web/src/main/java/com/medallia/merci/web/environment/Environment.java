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
package com.medallia.merci.web.environment;

import java.util.Map;
import java.util.Properties;

/**
 * Container for system environment variables and properties.
 */
public class Environment {

    private final String name;
    private final Map<String, String> systemVariables;
    private final Properties systemProperties;

    /**
     * Constructs environment container based on provided environment variables and system properties.
     *
     * @param name name of environment, i.e. "development", "qa", "production"
     * @param systemVariables system environment variables
     * @param systemProperties system properties
     */
    public Environment(String name, Map<String, String> systemVariables, Properties systemProperties) {
        this.name = name;
        this.systemVariables = systemVariables;
        this.systemProperties = systemProperties;
    }

    /**
     * @return name of enviroment.
     */
    public String getName() {
        return name;
    }

    /**
     * Return value of system variable as String.
     *
     * @param name name of system variable
     * @param defaultValue default value if no value found for system variable
     * @return value of system variable
     */
    public String getStringVariable(String name, String defaultValue) {
        String stringValue = systemVariables.get(name);
        if (stringValue == null) {
            return defaultValue;
        }
        return stringValue;
    }

    /**
     * Return value of system variable as int.
     *
     * @param name name of system variable
     * @param defaultValue default value if no value found for system variable
     * @return value of system variable as int
     */
    public int getIntVariable(String name, int defaultValue) {
        String stringValue = systemVariables.get(name);
        if (stringValue == null) {
            return defaultValue;
        }
        return Integer.parseInt(stringValue);
    }

    /**
     * Return value of system variable as String.
     *
     * @param name name of system variable
     * @param defaultValue default value if no value found for system variable
     * @return value of system variable as String
     */
    public String getStringProperty(String name, String defaultValue) {
        return systemProperties.getProperty(name, defaultValue);
    }
}
