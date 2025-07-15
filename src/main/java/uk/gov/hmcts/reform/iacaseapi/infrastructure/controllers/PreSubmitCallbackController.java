package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.ResponseEntity.ok;

import jakarta.validation.constraints.NotNull;
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
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.PreSubmitCallbackDispatcher;

@Slf4j
@Tag(name = "Asylum Service")

@RequestMapping(
    path = "/asylum",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RestController
public class PreSubmitCallbackController {

    private final PreSubmitCallbackDispatcher<AsylumCase> callbackDispatcher;

    public PreSubmitCallbackController(PreSubmitCallbackDispatcher<AsylumCase> callbackDispatcher) {
        requireNonNull(callbackDispatcher, "callbackDispatcher must not be null");

        this.callbackDispatcher = callbackDispatcher;
    }

    @Operation(
            summary = "Handles 'AboutToStartEvent' callbacks from CCD",
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

    @PostMapping(path = "/ccdAboutToStart")
    public ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> ccdAboutToStart(
        @Parameter(name = "Asylum case data", required = true) @NotNull @RequestBody Callback<AsylumCase> callback
    ) {
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_START, callback);
    }

    @Operation(
            summary = "Handles 'AboutToSubmitEvent' callbacks from CCD",
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



    @PostMapping(path = "/ccdAboutToSubmit")
    public ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> ccdAboutToSubmit(
        @Parameter(name = "Asylum case data", required = true) @NotNull @RequestBody Callback<AsylumCase> callback
    ) {
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
    }


    @Operation(
            summary = "Handles 'midEvent' callbacks from CCD",
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


    @PostMapping(path = "/ccdMidEvent")
    public ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> ccdMidEvent(
        @Parameter(name = "Asylum case data", required = true) @NotNull @RequestBody Callback<AsylumCase> callback,
        @RequestParam(name = "pageId", required = false) String pageId
    ) {
        if (pageId != null) {
            callback.setPageId(pageId);
        }
        return performStageRequest(PreSubmitCallbackStage.MID_EVENT, callback);
    }

    private ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> performStageRequest(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        log.info(
            "Asylum Case CCD `{}` event `{}` received for Case ID `{}`",
            callbackStage,
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            callbackDispatcher.handle(callbackStage, callback);

        if (!callbackResponse.getErrors().isEmpty()) {
            log.warn(
                "Asylum Case CCD `{}` event `{}` handled for Case ID `{}` with errors `{}`",
                callbackStage,
                callback.getEvent(),
                callback.getCaseDetails().getId(),
                callbackResponse.getErrors()
            );
        } else {

            log.info(
                "Asylum Case CCD `{}` event `{}` handled for Case ID `{}`",
                callbackStage,
                callback.getEvent(),
                callback.getCaseDetails().getId()
            );
        }

        return ok(callbackResponse);
    }
}
