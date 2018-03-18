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

import com.medallia.merci.core.structure.Context;

/**
 * Configuration.
 *
 * @param <T> type of configuration
 */
public final class Configuration<T> {

    private final String name;
    private final Context<T> context;

    /**
     * Creates configuration with provided name and context definition.
     *
     * @param name name of configuration
     * @param context context definition
     */
    public Configuration(String name, Context<T> context) {
        this.name = name;
        this.context = context;
    }

    /**
     * @return name
     */
    String getName() {
        return name;
    }

    /**
     * @return context definition
     */
    Context<T> getContext() {
        return context;
    }

    /**
     * Returns value object based on provided configuration context.
     *
     * @param runtimeContext context
     * @return value object based on context
     */
    T getValue(ConfigurationContext runtimeContext) {
        return context.getValue(runtimeContext);
    }
}
