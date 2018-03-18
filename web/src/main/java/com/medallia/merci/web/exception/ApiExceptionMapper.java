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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Optional;

/**
 * Mapper to transform every {@link Exception} into an {@link ErrorModel} and render it as JSON.
 *
 * {@link WebApplicationException} uses the HTTP status code from the Exception. Any other exception
 * gets mapped to INTERNAL SERVER ERROR (500).
 */
public class ApiExceptionMapper implements ExceptionMapper<Exception> {

    /** Creates mapper for API exceptions. */
    public ApiExceptionMapper() {
        // Mapper with no fields.
    }

    @Override
    public Response toResponse(Exception exception) {
        return getResponseFromExceptionClass(exception).orElseGet(() -> {
            if (exception instanceof WebApplicationException) {
                return toWebApplicationExceptionResponse((WebApplicationException) exception);
            } else {
                return createInternalServerErrorResponse();
            }
        });
    }

    private Response createInternalServerErrorResponse() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

    /**
     * Translate the given {@link WebApplicationException} into a corresponding response
     *
     * @param exception WebApplicationException
     * @return a response created from the {@link WebApplicationException}
     */
    private Response toWebApplicationExceptionResponse(WebApplicationException exception) {
        return Response.status(exception.getResponse().getStatus())
                .entity(toErrorModel(ErrorTypeModel.WEB_APP_ERROR, exception.getMessage()))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

    /**
     * Map Exception to Response.
     *
     * @param exception exception triggering the error response.
     * @return option with response set if its a known exception, empty optional otherwise.
     */
    private Optional<Response> getResponseFromExceptionClass(Exception exception) {
        if (exception instanceof InvalidInputException) {
            return Optional.of(produceResponse(Response.Status.BAD_REQUEST, ErrorTypeModel.INVALID_INPUT, exception));
        } else if (exception instanceof NotFoundException) {
            return Optional.of(produceResponse(Response.Status.NOT_FOUND, ErrorTypeModel.NOT_FOUND, exception));
        }
        return Optional.empty();
    }

    /**
     * Produces a {@link Response} using JSON as media type for an occurred {@link Exception}.
     *
     * @param status The response's {@link Response.Status}
     * @param errorTypeModel The {@link ErrorTypeModel} for the response.
     * @param exception The occurred {@link Exception}
     * @return The created {@link Response}.
     */
    private Response produceResponse(Response.Status status, ErrorTypeModel errorTypeModel, Exception exception) {
        return Response.status(status)
                .entity(toErrorModel(errorTypeModel, exception.getMessage()))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

    /**
     * Error Model for error response.
     *
     * @param errorType The {@link ErrorTypeModel} for the response.
     * @param message error message for the response.
     * @return Error Model for error response.
     */
    private ErrorModel toErrorModel(ErrorTypeModel errorType, String message) {
        return new ErrorModel(errorType, message);
    }
}
