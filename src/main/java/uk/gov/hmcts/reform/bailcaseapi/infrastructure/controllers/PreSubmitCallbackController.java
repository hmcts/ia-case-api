package uk.gov.hmcts.reform.bailcaseapi.infrastructure.controllers;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;

import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.PreSubmitCallbackDispatcher;

@Slf4j
@Api(
    value = "/bail",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequestMapping(
    path = "/bail",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RestController
public class PreSubmitCallbackController {

    private final PreSubmitCallbackDispatcher<BailCase> callbackDispatcher;

    public PreSubmitCallbackController(PreSubmitCallbackDispatcher<BailCase> callbackDispatcher) {
        requireNonNull(callbackDispatcher, "callbackDispatcher can not be null");
        this.callbackDispatcher = callbackDispatcher;
    }

    @ApiOperation(
        value = "Handles 'AboutToStartEvent' callbacks from CCD",
        response = PreSubmitCallbackResponse.class,
        authorizations = {
            @Authorization(value = "Authorization"),
            @Authorization(value = "ServiceAuthorization")
        }
    )

    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Transformed Bail case data, with any identified error or warning messages",
            response = PreSubmitCallbackResponse.class
            ),
        @ApiResponse(
            code = 400,
            message = "Bad Request"
            ),
        @ApiResponse(
            code = 403,
            message = "Forbidden"
            ),
        @ApiResponse(
            code = 415,
            message = "Unsupported Media Type"
            ),
        @ApiResponse(
            code = 500,
            message = "Internal Server Error"
            )
    })

    @PostMapping(path = "/ccdAboutToStart")
    public ResponseEntity<PreSubmitCallbackResponse<BailCase>> ccdAboutToStart(
        @ApiParam(value = "Bail case data", required = true) @NotNull @RequestBody Callback<BailCase> callback
    ) {
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_START, callback);
    }

    @ApiOperation(
        value = "Handles 'AboutToSubmitEvent' callbacks from CCD",
        response = PreSubmitCallbackResponse.class,
        authorizations = {
            @Authorization(value = "Authorization"),
            @Authorization(value = "ServiceAuthorization")
        }
    )
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Transformed Bail case data, with any identified error or warning messages",
            response = PreSubmitCallbackResponse.class
            ),
        @ApiResponse(
            code = 400,
            message = "Bad Request"
            ),
        @ApiResponse(
            code = 403,
            message = "Forbidden"
            ),
        @ApiResponse(
            code = 415,
            message = "Unsupported Media Type"
            ),
        @ApiResponse(
            code = 500,
            message = "Internal Server Error"
            )
    })

    @PostMapping(path = "/ccdAboutToSubmit")
    public ResponseEntity<PreSubmitCallbackResponse<BailCase>> ccdAboutToSubmit(
        @ApiParam(value = "Bail case data", required = true) @NotNull @RequestBody Callback<BailCase> callback
    ) {
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
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
