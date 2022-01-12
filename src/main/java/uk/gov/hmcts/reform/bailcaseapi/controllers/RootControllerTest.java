package uk.gov.hmcts.reform.bailcaseapi.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RootControllerTest {

    private final RootController rootController = new RootController();

    @Test
    public void should_return_welcome_response() {

        ResponseEntity<String> responseEntity = rootController.welcome();

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(
            "Welcome to the Bail case API",
            responseEntity.getBody()
        );
    }
}
