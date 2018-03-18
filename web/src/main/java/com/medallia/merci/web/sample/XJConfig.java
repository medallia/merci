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
package com.medallia.merci.web.sample;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Runtime config for XJ service.
 */
public class XJConfig {

    private final List<String> hosts;
    private final int port;
    private final Duration timeout;
    private final String description;

    public XJConfig() {
        this(Arrays.asList("invalid-host"), -1, -1, "hard-coded invalid config");
    }

    @JsonCreator
    public XJConfig(@JsonProperty("hosts") List<String> configServiceHosts,
                    @JsonProperty("port") int port,
                    @JsonProperty("timeoutSeconds") int timeoutSeconds,
                    @JsonProperty("description") String description) {
        this.hosts = configServiceHosts;
        this.port = port;
        timeout = Duration.ofSeconds(timeoutSeconds);
        this.description = description;
    }

    @JsonProperty("hosts")
    public List<String> getHosts() {
        return hosts;
    }

    @JsonProperty("port")
    public int getPort() {
        return port;
    }

    @JsonProperty("timeoutSeconds")
    public long getTimeoutSeconds() {
        return timeout.getSeconds();
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }
}