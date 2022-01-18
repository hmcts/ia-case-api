package uk.gov.hmcts.reform.bailcaseapi.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Default endpoints per application
 */

@RestController
public class RootController {

    /**
     * Root GET endpoint.
     *
     * @return welcome message from the service
     */

    @GetMapping(value = "/")
    public ResponseEntity<String> welcome() {
        return ok("Welcome to the Bail case API");
    }
}
