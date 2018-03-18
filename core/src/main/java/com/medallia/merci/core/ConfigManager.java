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

import com.medallia.merci.core.exception.ConfigInstantiationException;

/**
 * In-memory store for mixed-type configurations.
 */
public class ConfigManager extends AbstractConfigurationManager<Object> {

    /**
     * Creates new config manager.
     */
    public ConfigManager() {
        super();
    }

    /**
     * Return config value object of class T for given request (runtime) config context.
     *
     * @param clazz Java class of config to be evaluated
     * @param runtimeContext context from request to be used for evaluation of config
     * @param <T> class of config
     * @return config value object from config store or clazz.newInstance()
     * @throws ConfigInstantiationException in case of instantiation problems
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public <T> T getConfig(Class<T> clazz, ConfigurationContext runtimeContext) throws ConfigInstantiationException {
        Object value = getValue(clazz.getName(), runtimeContext, null);
        if (value != null) {
            try {
                return clazz.cast(value);
            } catch (ClassCastException exception) {
                // try to return new config object based on default constructor
            }
        }
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException exception) {
            throw new ConfigInstantiationException(exception);
        }
    }
}
