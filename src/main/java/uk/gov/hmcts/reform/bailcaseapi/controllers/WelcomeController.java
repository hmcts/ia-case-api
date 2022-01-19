package uk.gov.hmcts.reform.bailcaseapi.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

@Api(
    value = "/",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RestController
public class WelcomeController {

    private static final Logger LOG = getLogger(WelcomeController.class);
    private static final String INSTANCE_ID = UUID.randomUUID().toString();
    private static final String MESSAGE = "Welcome to Bail case API";

    /**
     * Root GET endpoint.
     * @return welcome message from the service
     */

    @ApiOperation("Welcome message for the Immigration & Asylum case API")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Welcome message",
            response = String.class
            )
    })

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> welcome() {
        LOG.info("Welcome message '{}' from running instance: {}", MESSAGE, INSTANCE_ID);
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noCache())
            .body("{\"message\": \"" + MESSAGE + "\"}");
    }
}
