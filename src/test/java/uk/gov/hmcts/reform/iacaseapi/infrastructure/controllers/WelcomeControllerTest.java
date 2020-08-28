package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class WelcomeControllerTest {

    final WelcomeController welcomeController = new WelcomeController();

    @Test
    void should_return_welcome_response() {

        ResponseEntity<String> responseEntity = welcomeController.welcome();

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertThat(
            responseEntity.getBody())
            .contains("Welcome to Immigration & Asylum case API");
    }
}
