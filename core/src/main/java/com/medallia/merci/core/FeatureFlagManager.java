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

/**
 * In-memory store for runtime feature flags.
 */
public class FeatureFlagManager extends AbstractConfigurationManager<Boolean> {

    /**
     * Creates new feature flag manager.
     */
    public FeatureFlagManager() {
        super();
    }

    /**
     * Returns true if feature flag with given name (case-sensitive) was found and is active for provided runtime configuration context,
     * false otherwise.
     *
     * @param name name of feature flag to be evaluated, case-sensitive
     * @param runtimeContext configuration context from request to be used for evaluation of feature flag
     * @return true if feature flag was found and is active
     */
    public boolean isActive(String name, ConfigurationContext runtimeContext) {
        return isActive(name, runtimeContext, false);
    }

    /**
     * Return true if feature flag with given name (case-sensitive) was found and is active for provided runtime configuration context,
     * default value if feature flag could not be found.
     *
     * @param name name of feature flag to be evaluate, case-sensitive
     * @param runtimeContext configuration context from request to be used for evaluation of feature flag
     * @param defaultValue default to be return in feature flag manager does not contain feature flag
     * @return true or false if feature flag was found and is active or inactive, default value if feature flag could not be found.
     */
    public boolean isActive(String name, ConfigurationContext runtimeContext, boolean defaultValue) {
        return getValue(name, runtimeContext, Boolean.valueOf(defaultValue)).booleanValue();
    }
}
