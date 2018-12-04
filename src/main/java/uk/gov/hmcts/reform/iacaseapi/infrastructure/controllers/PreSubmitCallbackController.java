package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.annotations.*;
import javax.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.PreSubmitCallbackDispatcher;

@Api(
    value = "/asylum",
    consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
@RequestMapping(
    path = "/asylum",
    consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
@RestController
public class PreSubmitCallbackController {

    private static final org.slf4j.Logger LOG = getLogger(PreSubmitCallbackController.class);

    private final PreSubmitCallbackDispatcher<AsylumCase> callbackDispatcher;

    public PreSubmitCallbackController(
        PreSubmitCallbackDispatcher<AsylumCase> callbackDispatcher
    ) {
        requireNonNull(callbackDispatcher, "callbackDispatcher must not be null");

        this.callbackDispatcher = callbackDispatcher;
    }

    @ApiOperation(
        value = "Handles 'AboutToStartEvent' callbacks from CCD",
        response = PreSubmitCallbackResponse.class,
        authorizations =
            {
                @Authorization(value = "Authorization"),
                @Authorization(value = "ServiceAuthorization")
            }
    )

    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Transformed Asylum case data, with any identified error or warning messages",
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
    public ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> ccdAboutToStart(
        @ApiParam(value = "Asylum case data", required = true) @NotNull @RequestBody Callback<AsylumCase> callback
    ) {
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_START, callback);
    }

    @ApiOperation(
        value = "Handles 'AboutToSubmitEvent' callbacks from CCD",
        response = PreSubmitCallbackResponse.class,
        authorizations =
            {
                @Authorization(value = "Authorization"),
                @Authorization(value = "ServiceAuthorization")
            }
    )
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Transformed Asylum case data, with any identified error or warning messages",
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
    public ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> ccdAboutToSubmit(
        @ApiParam(value = "Asylum case data", required = true) @NotNull @RequestBody Callback<AsylumCase> callback
    ) {
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
    }

    private ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> performStageRequest(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        LOG.info(
            "Asylum Case CCD `{}` event received for Case ID `{}`",
            callbackStage,
            callback.getCaseDetails().getId()
        );

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            callbackDispatcher.handle(callbackStage, callback);

        LOG.info(
            "Asylum Case CCD `{}` event handled for Case ID `{}`",
            callbackStage,
            callback.getCaseDetails().getId()
        );

        return ok(callbackResponse);
    }
}
