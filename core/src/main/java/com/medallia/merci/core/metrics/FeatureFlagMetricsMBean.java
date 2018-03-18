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
 * Bean for metrics of feature flags.
 */
public interface FeatureFlagMetricsMBean {

    /** @retun total number of skipped update cycles of feature flags due to same configuration content. */
    long getFeatureFlagSameContentsSkips();

    /** @retun total number of successful update cycles of feature flags due to new configuration content. */
    long getFeatureFlagNewContentsUpdates();

    /** @retun total number of successful feature flag updates. */
    long getFeatureFlagUpdates();

    /** @retun total number of failures reading feature flags due due to problems parsing configuration content. */
    long getFeatureFlagContentFailures();

    /** @retun total number of skipped feature flags due to problems instantiating Java configuration objects. */
    long getFeatureFlagNonInstantiableSkips();

    /** @retun total number of duplicate feature flag name detections. */
    long getFeatureFlagNameDuplicates();
}
