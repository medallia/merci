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
package com.medallia.merci.web.configs;

import com.medallia.merci.core.JsonConfigManager;
import org.glassfish.hk2.api.Factory;

public class ConfigControllerFactory implements Factory<ConfigController> {

    private final JsonConfigManager jsonConfigManager;

    public ConfigControllerFactory(JsonConfigManager jsonConfigManager) {
        this.jsonConfigManager = jsonConfigManager;
    }

    @Override
    public ConfigController provide() {
        return new ConfigController(jsonConfigManager);
    }

    @Override
    public void dispose(ConfigController instance) {
        //Nothing to do.
    }
}
