package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Api(
    value = "/",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)

@RestController
public class WelcomeController {

    /**
     * Root GET endpoint.
     *
     * <p>Azure application service has a hidden feature of making requests to root endpoint when
     * "Always On" is turned on.
     * This is the endpoint to deal with that and therefore silence the unnecessary 404s as a response code.
     *
     * @return Welcome message from the service.
     */
    @ApiOperation("Welcome message for the Immigration & Asylum case API")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Welcome message",
            response = String.class
        )
    })
    @GetMapping(
        path = "/",
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public ResponseEntity<String> welcome() {
        return ResponseEntity
            .ok()
            .cacheControl(CacheControl.noCache())
            .body("{\"message\": \"Welcome to Immigration & Asylum case API\"}");
    }

}
