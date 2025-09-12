package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.PostSubmitCallbackDispatcher;


@Slf4j
@Tag(name = "Asylum Service")

@RequestMapping(
    path = "/asylum",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RestController
public class PostSubmitCallbackController {

    private final PostSubmitCallbackDispatcher<AsylumCase> callbackDispatcher;

    public PostSubmitCallbackController(
        PostSubmitCallbackDispatcher<AsylumCase> callbackDispatcher
    ) {
        requireNonNull(callbackDispatcher, "callbackDispatcher must not be null");

        this.callbackDispatcher = callbackDispatcher;
    }

    @Operation(
            summary = "Handles 'SubmittedEvent' callbacks from CCD",
            security =
                {
                @SecurityRequirement(name = "Authorization"),
                @SecurityRequirement(name = "ServiceAuthorization")
                },
            responses =
                {
                @ApiResponse(
                      responseCode = "200",
                      description = "Optional confirmation text for CCD UI",
                      content = @Content(schema = @Schema(implementation = PostSubmitCallbackResponse.class))),
                @ApiResponse(
                      responseCode = "400",
                      description = "Bad Request",
                      content = @Content(schema = @Schema(implementation = PostSubmitCallbackResponse.class))),
                @ApiResponse(
                      responseCode = "403",
                      description = "Forbidden",
                      content = @Content(schema = @Schema(implementation = PostSubmitCallbackResponse.class))),
                @ApiResponse(
                      responseCode = "415",
                      description = "Unsupported Media Type",
                      content = @Content(schema = @Schema(implementation = PostSubmitCallbackResponse.class))),
                @ApiResponse(
                      responseCode = "500",
                      description = "Internal Server Error",
                      content = @Content(schema = @Schema(implementation = PostSubmitCallbackResponse.class)))
                }
    )

    @PostMapping(path = "/ccdSubmitted")
    public ResponseEntity<PostSubmitCallbackResponse> ccdSubmitted(
        @Parameter(name = "Asylum case data", required = true) @RequestBody Callback<AsylumCase> callback
    ) {
        log.info(
            "Asylum Case CCD `ccdSubmitted` event `{}` received for Case ID `{}`",
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );

        PostSubmitCallbackResponse callbackResponse =
            callbackDispatcher.handle(callback);

        log.info(
            "AAAsylum Case CCD `ccdSubmitted` event `{}` handled for Case ID `{}`",
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );

        return ok(callbackResponse);
    }
}
