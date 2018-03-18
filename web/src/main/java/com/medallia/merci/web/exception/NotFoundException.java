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

public class NotFoundException extends RuntimeException {

    /** Error Type to keep the state of the Exception. */
    private final ErrorType type;

    /**
     * Creates exception.
     *
     * @param errorType Error type for the exception.
     */
    public NotFoundException(ErrorType errorType) {
        super();
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
        CONFIGS,
        FEATURE_FLAGS
    }

    @Override
    public String getMessage(){
        return type.toString();
    }
}
