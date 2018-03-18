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

import java.util.concurrent.atomic.LongAdder;

/**
 * Metrics container for configs.
 */
public class ConfigMetrics implements ConfigMetricsMBean, UpdateConfigurationMetrics, InstantiateConfigurationMetrics {

    private final LongAdder sameContentsSkips;
    private final LongAdder newContentsUpdates;
    private final LongAdder updates;
    private final LongAdder contentFailures;
    private final LongAdder nonInstantiableSkips;
    private final LongAdder nameDuplicates;

    /**
     * Creates metrics container for configs.
     */
    public ConfigMetrics() {
        sameContentsSkips = new LongAdder();
        newContentsUpdates = new LongAdder();
        contentFailures = new LongAdder();
        updates = new LongAdder();
        nonInstantiableSkips = new LongAdder();
        nameDuplicates = new LongAdder();
    }

    @Override
    public void incrementSameContentsSkips() {
        sameContentsSkips.increment();
    }

    @Override
    public void incrementNewContentsUpdates() {
        newContentsUpdates.increment();
    }

    @Override
    public void incrementUpdates(long value) {
        updates.add(value);
    }

    @Override
    public void incrementContentFailures(long value) {
        contentFailures.add(value);
    }

    @Override
    public void incrementNameDuplicates(long value) {
        nameDuplicates.add(value);
    }

    @Override
    public void incrementNonInstantiableSkips() {
        nonInstantiableSkips.increment();
    }

    @Override
    public long getConfigSameContentsSkips() {
        return sameContentsSkips.sum();
    }

    @Override
    public long getConfigNewContentsUpdates() {
        return newContentsUpdates.sum();
    }

    @Override
    public long getConfigUpdates() {
        return updates.sum();
    }

    @Override
    public long getConfigContentFailures() {
        return contentFailures.sum();
    }

    @Override
    public long getConfigNonInstantiableSkips() {
        return nonInstantiableSkips.sum();
    }

    @Override
    public long getConfigNameDuplicates() {
        return nameDuplicates.sum();
    }
}
