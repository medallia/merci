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
 * Represents a server error caused by something outside of the control of this application.
 * <p>
 * This error is temporary and the client should retry the operation in order to get a successful response.
 */
public class TemporaryErrorException extends RuntimeException {

    /**
     * Creates exception.
     *
     * @param message Basic message for the client of the application.
     */
    public TemporaryErrorException(String message) {
        super(message);
    }

    /**
     * Creates exception.
     *
     * @param message Basic message for the client of the application.
     * @param cause   The original exception that caused this error.
     */
    public TemporaryErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
