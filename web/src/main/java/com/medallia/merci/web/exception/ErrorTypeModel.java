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
 * The different types of error
 **/
public enum ErrorTypeModel {
    NOT_FOUND("not_found"),
    INVALID_INPUT("invalid_input"),
    WEB_APP_ERROR("web_application_error"),
    INTERNAL_SERVER_ERROR("internal_server_error");

    private final String value;

    /**
     * Constructor.
     * @param value value of the error type.
     */
    private ErrorTypeModel(String value) {
        this.value = value;
    }

    @Override
    @com.fasterxml.jackson.annotation.JsonValue
    public String toString() {
        return value;
    }
}
