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
package com.medallia.merci.web.exception;

/**
 * Represents a client error caused by the invalid state of an input.
 * <p>
 * Different types of errors can be modeled by subclassing this class of adding an object that
 * represents the type of the error.
 * <p>
 * In general, clients want to get the maximum quantity of errors they made in the response.
 * Consider adding a list of errors in the state of this object.
 */
public class InvalidInputException extends RuntimeException {

    /** Error Type to keep the state of the Exception. */
    private final ErrorType type;

    /**
     * Creates exception.
     *
     * @param errorType Error type for the exception.
     */
    public InvalidInputException(ErrorType errorType) {
        super();
        type = errorType;
    }

    /**
     * Creates exception.
     *
     * @param errorType error type for the exception.
     * @param exception throwable wrapped in this exception
     */
    public InvalidInputException(ErrorType errorType, Throwable exception) {
        super(exception);
        type = errorType;
    }

    /** @return the error type associated with the exception. */
    public ErrorType getErrorType() {
        return type;
    }

    /**
     * Error Type enum to add the state to the Exception.
     */
    public enum ErrorType {
        CONFIG_ID,
        FEATURE_FLAG_ID
    }

    @Override
    public String getMessage() {
        return type.toString();
    }
}
