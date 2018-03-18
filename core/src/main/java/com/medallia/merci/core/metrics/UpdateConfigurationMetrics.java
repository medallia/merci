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
package com.medallia.merci.core.metrics;

/**
 * Metrics container for feature flags.
 */
public interface UpdateConfigurationMetrics {

    /** Increment counter for successful updates of configurations. */
    void incrementUpdates(long value);

    /** Increment counter for failed updates of configurations due to deserialization problems with textual content. */
    void incrementContentFailures(long value);

    /** Increment number of skipped update cycles of configurations due to same textual contents. */
    void incrementSameContentsSkips();

    /** Increment number of successful update cycles of configurations due to new textual contents. */
    void incrementNewContentsUpdates();

    /** Increment counter for duplicate configuration name detections. */
    void incrementNameDuplicates(long value);
}
