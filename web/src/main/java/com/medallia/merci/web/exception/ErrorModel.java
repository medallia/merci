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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Error Model used in error response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("error")
public class ErrorModel {

    @JsonProperty("error_type")
    private final ErrorTypeModel errorType;

    @JsonProperty("message")
    private final String message;

    /**
     * Creates error model.
     *
     * @param errorType type of error.
     * @param message error message
     */
    public ErrorModel(ErrorTypeModel errorType, String message) {
        this.errorType = errorType;
        this.message = message;
    }

    /**
     * @return error type model.
     */
    @JsonProperty("error_type")
    public ErrorTypeModel getErrorType() {
        return errorType;
    }

    /**
     * @return error message.
     */
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }
}
