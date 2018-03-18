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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.medallia.merci.core.ConfigurationContext;

/**
 * A configuration context is a sub-tree in the definition of a configuration, that defines a (default) value object of type T
 * at the current level, and an optional override hierarchy, called modifiers.
 *
 * I.e., in the following JSON representation of a feature flag configuration, the top default value (object)
 * of type Boolean is 'false' and the modifiers hierarchy is defined to override the default value depending
 * on an environment value.
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
 * @param <T> type of value object
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"comment"})
public class Context<T> {

    /** Mandatory, default value object. */
    @JsonProperty("value")
    private final T value;

    /** Optional modifiers to override default value object, nullable. */
    @JsonProperty("modifiers")
    private final Modifiers<T> modifiers;

    /**
     * Creates context based on mandatory (default) value object and optional modifiers.
     *
     * @param value mandatory default value object
     * @param modifiers optional modifiers to override default, nullable
     */
    @JsonCreator
    public Context(@JsonProperty(value = "value", required = true) T value,
                   @JsonProperty(value = "modifiers") Modifiers<T> modifiers) {
        this.value = value;
        this.modifiers = modifiers;
    }

    /**
     * Returns value object based on provided runtime context.
     *
     * @param runtimeContext context, provided at runtime
     * @return value object based on runtime context
     */
    public T getValue(ConfigurationContext runtimeContext) {
        if (modifiers != null) {
            T modifiersVaue = modifiers.getValue(runtimeContext);
            if (modifiersVaue != null) {
                return modifiersVaue;
            }
        }
        return value;
    }
}
