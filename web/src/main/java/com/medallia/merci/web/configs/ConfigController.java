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
import com.medallia.merci.web.QueryParameters;
import com.medallia.merci.core.ConfigurationContext;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

/**
 * Controller (Jersey resource) for Config API.
 */
@Consumes("application/json")
@Produces("application/json")
@Path("/apis/v0/configs")
public class ConfigController {

    private final JsonConfigManager jsonConfigManager;

    /**
     * Creates controller for configs.
     */
    public ConfigController(JsonConfigManager jsonConfigManager) {
        this.jsonConfigManager = jsonConfigManager;
    }

    /**
     * Returns evaluated Json Config object based on provided id and query parameters.
     *
     * @param configId id of config
     * @param queryParameters query parameters with configuration context mappings
     * @return evaluated Json config
     */
    @GET
    @Path("/{id}")
    public Config get(@PathParam("id") String configId, @Context QueryParameters queryParameters) {
        ConfigurationContext configurationContext = new ConfigurationContext();
        for (String key : queryParameters.keySet()) {
            configurationContext.put(key, queryParameters.get(key));
        }
        return new Config(configId, jsonConfigManager.getConfig(configId, configurationContext));
    }
}
