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
package com.medallia.merci.core.structure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.medallia.merci.core.ConfigurationContext;

import java.util.Map;

/**
 * A configuration modifiers is an override hierarchy in the definition of a configuration.
 *
 * I.e., in the following JSON representation of a feature flag configuration, the modifiers hierarchy is defined to override
 * the default value (object) 'false' with 'true' for (runtime) context values 'qa' and 'prod' of type 'environment'.
 *
 *  "enable-feature-one": {
 *      "value": false,
 *      "modifiers": {
 *          "type": "environment",
 *          "contexts": {
 *              "qa": {
 *                  "value": true
 *              },
 *              "prod": {
 *                  "value": true
 *              }
 *          }
 *      }
 *  }
 *
 * @param <T> type of context
 */
public class Modifiers<T> {

    /** Type of context values to override default value from parent level in configuration hierarchy. */
    @JsonProperty("type")
    private final String type;

    /** Map of context value to configuration context. */
    @JsonProperty("contexts")
    private final Map<String, Context<T>> contexts;

    /**
     * Creates new modifiers container for a given context type and a mapping of context values new contexts.
     *
     * @param type context type
     * @param contexts map of context value to context object
     */
    @JsonCreator
    public Modifiers(@JsonProperty(value = "type", required = true) String type,
                     @JsonProperty(value = "contexts", required = true) Map<String, Context<T>> contexts) {
        this.type = type;
        this.contexts = contexts;
    }

    /**
     * Returns config value (object) for a given (runtime) context, null if none found.
     *
     * @param runtimeContext configuration context at runtime
     * @return configuration value of type T for given configuration context, null if none found.
     */
    public T getValue(ConfigurationContext runtimeContext) {
        String runtimeContextValue = runtimeContext.get(type);
        if (runtimeContextValue == null) {
            return null;
        }
        Context<T> context = contexts.get(runtimeContextValue);
        if (context == null) {
            return null;
        }
        return context.getValue(runtimeContext);
    }
}
