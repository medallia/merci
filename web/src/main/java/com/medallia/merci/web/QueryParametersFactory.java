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
package com.medallia.merci.web;

import org.glassfish.hk2.api.Factory;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.LinkedHashMap;

public class QueryParametersFactory implements Factory<QueryParameters> {

    private final UriInfo uriInfo;

    /**
     * Constructs.
     */
    public QueryParametersFactory(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @Override
    public QueryParameters provide() {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (String key : queryParameters.keySet()) {
            map.put(key, queryParameters.getFirst(key));
        }
        return new QueryParameters(map);
    }

    @Override
    public void dispose(QueryParameters instance) {
        //Nothing to do.
    }
}