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
package com.medallia.merci.core.fetcher;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Fetcher for local and remote configuration files.
 */
public interface ConfigurationFetcher {

    /**
     * Return map of configuration content from local or remote files.
     *
     * @param fileNames names of configuration files
     * @param application name of application
     * @return map with file names and configuration content
     * @throws IOException in case of a failure
     */
    Map<String, String> fetch(List<String> fileNames, String application) throws IOException;
}
