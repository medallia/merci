package com.medallia.merci.core.filesystem;

/**
 * Bean for metrics of configuration fetcher.
 */
public interface ConfigurationFetcherMetricsMBean {

    /**
     * @return total count of requests fetching content of configuration files, failed and successful.
     */
    long getRequests();

    /**
     * @return total count of failed requests fetching content of configuration files.
     */
    long getFailures();

    /**
     * @return total count of missing configuration files.
     */
    long getMissingFiles();
}
