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
package com.medallia.merci.core.filesystem;

import com.medallia.merci.core.fetcher.ConfigurationFetcher;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration fetcher for the local file system.
 */
public class FilesystemConfigurationFetcher implements ConfigurationFetcher {

    private final FileSystem fileSystem;
    private final String basePath;
    private final boolean skipMissingFiles;
    private final ConfigurationFetcherMetrics metrics;
//    private final int i = 1; // FINDBUGS VIOLATION 'SS' (SS_SHOULD_BE_STATIC)
//
//    public Boolean returnNullBoolean() {
//        String npNull = null;
//        int len = npNull.length();
//        return null;
//    }

    /**
     * Creates a configuration fetcher for the local file system.
     *
     * @param fileSystem file system for access to local configuration files
     * @param basePath base path of configuration files on file system
     * @param skipMissingFiles true, if fetcher should continue despite missing files
     * @param metrics metrics
     */
    public FilesystemConfigurationFetcher(FileSystem fileSystem,
                                          String basePath,
                                          boolean skipMissingFiles,
                                          ConfigurationFetcherMetrics metrics) {
        this.fileSystem = fileSystem;
        this.basePath = basePath;
        this.skipMissingFiles = skipMissingFiles;
        this.metrics = metrics;
    }

    @Override
    public Map<String, String> fetch(List<String> fileNames, String application) throws IOException {
        Map<String, String> contents = new LinkedHashMap<>();
        try {
            metrics.incrementRequests();
            for (String fileName : fileNames) {
                try {
                    String content = fetch(fileName, application);
                    contents.put(fileName, content);
                } catch (NoSuchFileException exception) {
                    metrics.incrementMissingFiles();
                    if (!skipMissingFiles) {
                        throw exception;
                    }
                }
            }
        } catch (IOException exception) {
            metrics.incrementFailures();
            throw exception;
        }
        return contents;
    }

    private String fetch(String fileName, String application) throws IOException {
        Path path = fileSystem.getPath(basePath + "/" + application + fileName);
        try (InputStream inputStream = fileSystem.provider().newInputStream(path, StandardOpenOption.READ)) {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        }
    }
}
