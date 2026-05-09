package com.sismics.rest.exception;

import jakarta.json.JsonObject;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionTest {
    @Test
    void shouldBuildBadRequestClientException() {
        ClientException exception = new ClientException("ValidationError", "field must be set");

        assertResponse(exception.getResponse(), 400, "ValidationError", "field must be set");
    }

    @Test
    void shouldBuildServerExceptionWithCause() {
        ServerException exception = new ServerException("FileError", "unable to read", new IllegalStateException("boom"));

        assertResponse(exception.getResponse(), 500, "FileError", "unable to read");
    }

    private void assertResponse(Response response, int status, String type, String message) {
        JsonObject json = (JsonObject) response.getEntity();
        assertEquals(status, response.getStatus());
        assertEquals(type, json.getString("type"));
        assertEquals(message, json.getString("message"));
    }
}
