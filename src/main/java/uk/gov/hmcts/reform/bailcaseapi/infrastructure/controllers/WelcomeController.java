package uk.gov.hmcts.reform.bailcaseapi.infrastructure.controllers;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Bail service")
@RestController
public class WelcomeController {

    private static final Logger LOG = getLogger(WelcomeController.class);
    private static final String INSTANCE_ID = UUID.randomUUID().toString();
    private static final String MESSAGE = "Welcome to the Bail case API";

    /**
     * Root GET endpoint.
     *
     * @return welcome message from the service
     */

    @Operation(
        summary = "Welcome message for the Immigration & Asylum Bail case API",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Welcome message",
                content = @Content(schema = @Schema(implementation = String.class)))}
    )
    @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> welcome() {
        LOG.info("Welcome message '{}' from running instance: {}", MESSAGE, INSTANCE_ID);
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noCache())
            .body("{\"message\": \"" + MESSAGE + "\"}");
    }
}
