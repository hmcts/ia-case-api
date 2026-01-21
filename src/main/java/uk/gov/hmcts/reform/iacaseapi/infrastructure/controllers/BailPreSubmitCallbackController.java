package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.ResponseEntity.ok;

import javax.validation.constraints.NotNull;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.PreSubmitCallbackDispatcher;

@Slf4j
@Tag(name = "Bail service")
@RequestMapping(
    path = "/bail",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RestController
public class BailPreSubmitCallbackController {

    private final PreSubmitCallbackDispatcher<BailCase> callbackDispatcher;

    public BailPreSubmitCallbackController(PreSubmitCallbackDispatcher<BailCase> callbackDispatcher) {
        requireNonNull(callbackDispatcher, "callbackDispatcher can not be null");
        this.callbackDispatcher = callbackDispatcher;
    }

    @Operation(
        summary = "Handles 'AboutToStartEvent' callbacks from CCD",
        security = {
            @SecurityRequirement(name = "Authorization"),
            @SecurityRequirement(name = "ServiceAuthorization")},
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Transformed Bail case data, with any identified error or warning messages",
                content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),
            @ApiResponse(
                responseCode = "400",
                description = "Bad Request"),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"),
            @ApiResponse(
                responseCode = "415",
                description = "Unsupported Media Type"),
            @ApiResponse(
                responseCode = "500",
                description = "Internal Server Error")}
    )

    @PostMapping(path = "/ccdAboutToStart")
    public ResponseEntity<PreSubmitCallbackResponse<BailCase>> ccdAboutToStart(
        @Parameter(name = "Bail case data", required = true) @NotNull @RequestBody Callback<BailCase> callback
    ) {
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_START, callback);
    }

    @Operation(
        summary = "Handles 'AboutToSubmitEvent' callbacks from CCD",
        security = {
            @SecurityRequirement(name = "Authorization"),
            @SecurityRequirement(name = "ServiceAuthorization")},
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Transformed Bail case data, with any identified error or warning messages",
                content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),
            @ApiResponse(
                responseCode = "400",
                description = "Bad Request"),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"),
            @ApiResponse(
                responseCode = "415",
                description = "Unsupported Media Type"),
            @ApiResponse(
                responseCode = "500",
                description = "Internal Server Error")}
    )
    @PostMapping(path = "/ccdAboutToSubmit")
    public ResponseEntity<PreSubmitCallbackResponse<BailCase>> ccdAboutToSubmit(
        @Parameter(name = "Bail case data", required = true) @NotNull @RequestBody Callback<BailCase> callback
    ) {
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
    }

    @Operation(
        summary = "Handles 'MidEventEvent' callbacks from CCD",
        security = {
            @SecurityRequirement(name = "Authorization"),
            @SecurityRequirement(name = "ServiceAuthorization")},
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Transformed Bail case data, with any identified error or warning messages",
                content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),
            @ApiResponse(
                responseCode = "400",
                description = "Bad Request"),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden"),
            @ApiResponse(
                responseCode = "415",
                description = "Unsupported Media Type"),
            @ApiResponse(
                responseCode = "500",
                description = "Internal Server Error")}
    )

    @PostMapping(path = "/ccdMidEvent")
    public ResponseEntity<PreSubmitCallbackResponse<BailCase>> ccdMidEvent(
        @Parameter(name = "Bail case data", required = true) @NotNull @RequestBody Callback<BailCase> callback,
        @RequestParam(name = "pageId", required = false) String pageId
    ) {
        if (pageId != null) {
            callback.setPageId(pageId);
        }
        return performStageRequest(PreSubmitCallbackStage.MID_EVENT, callback);
    }

    private ResponseEntity<PreSubmitCallbackResponse<BailCase>> performStageRequest(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        log.info(
            "Bail Case CCD `{}` event `{}` received for Case ID `{}`",
            callbackStage,
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            callbackDispatcher.handle(callbackStage, callback);

        if (!callbackResponse.getErrors().isEmpty()) {
            log.warn(
                "Bail Case CCD `{}` event `{}` handled for Case ID `{}` with errors `{}`",
                callbackStage,
                callback.getEvent(),
                callback.getCaseDetails().getId(),
                callbackResponse.getErrors()
            );
        } else {

            log.info(
                "Bail Case CCD `{}` event `{}` handled for Case ID `{}`",
                callbackStage,
                callback.getEvent(),
                callback.getCaseDetails().getId()
            );
        }

        return ok(callbackResponse);
    }
}
