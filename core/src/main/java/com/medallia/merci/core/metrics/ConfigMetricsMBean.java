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
 * Bean for metrics of configs.
 */
public interface ConfigMetricsMBean {

    /** @retun total number of skipped update cycles of configs due to same configuration content. */
    long getConfigSameContentsSkips();

    /** @retun total number of successful update cycles of configs due to new or modified configuration content. */
    long getConfigNewContentsUpdates();

    /** @retun total number of successful Java config updates after reading and deserializing configs. */
    long getConfigUpdates();

    /** @retun total number of failures reading configs due to problems parsing configuration content. */
    long getConfigContentFailures();

    /** @retun total number of skipped configs due to problems instantiating Java configuration objects. */
    long getConfigNonInstantiableSkips();

    /** @retun total number of duplicate config name detections. */
    long getConfigNameDuplicates();
}
