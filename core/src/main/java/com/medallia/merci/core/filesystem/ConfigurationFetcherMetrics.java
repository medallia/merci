package com.medallia.merci.core.filesystem;

import java.util.concurrent.atomic.LongAdder;

/**
 * Metrics container for the {@link com.medallia.merci.core.fetcher.ConfigurationFetcher}.
 * Implements mbean defined in {@link ConfigurationFetcherMetricsMBean}.
 */
public class ConfigurationFetcherMetrics implements ConfigurationFetcherMetricsMBean {

    /** Counter for requests, failed and successful. */
    private final LongAdder requests;

    /** Counter for failed requests . */
    private final LongAdder failures;

    /** Counter for missing files. */
    private final LongAdder numMissingFiles;

    /**
     * Creates metrics container.
     */
    public ConfigurationFetcherMetrics() {
        requests = new LongAdder();
        failures = new LongAdder();
        numMissingFiles = new LongAdder();
    }

    /**
     * Increment counter for requests, failed and successful.
     */
    public void incrementRequests() {
        requests.increment();
    }

    /**
     * Increment counter for failed requests.
     */
    public void incrementFailures() {
        failures.increment();
    }

    /**
     * Increment counter for missing files.
     */
    public void incrementMissingFiles() {
        numMissingFiles.increment();
    }

    @Override
    public long getRequests() {
        return requests.sum();
    }

    @Override
    public long getFailures() {
        return failures.sum();
    }

    @Override
    public long getMissingFiles() {
        return numMissingFiles.sum();
    }
}
