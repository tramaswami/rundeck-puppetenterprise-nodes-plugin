package com.rundeck.plugin.resources.puppetdb.client;

import com.google.common.base.Function;

/**
 * interface for making http requests and parsing results
 */
public interface HTTP {
    /**
     * Make a request to a path, use the parser for the result
     *
     * @param path        path
     * @param parser      parser of data
     * @param errResponse result if any error
     * @param name        logger name
     * @param <T>         result type
     *
     * @return result
     */
    <T> T makeRequest(final String path, Function<String, T> parser, T errResponse, String name);
}
