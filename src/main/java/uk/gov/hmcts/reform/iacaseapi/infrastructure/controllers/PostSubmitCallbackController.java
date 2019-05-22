package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.annotations.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.PostSubmitCallbackDispatcher;


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
public class PostSubmitCallbackController {

    private static final org.slf4j.Logger LOG = getLogger(PostSubmitCallbackController.class);

    private final PostSubmitCallbackDispatcher<CaseDataMap> callbackDispatcher;

    public PostSubmitCallbackController(
        PostSubmitCallbackDispatcher<CaseDataMap> callbackDispatcher
    ) {
        requireNonNull(callbackDispatcher, "callbackDispatcher must not be null");

        this.callbackDispatcher = callbackDispatcher;
    }

    @ApiOperation(
        value = "Handles 'SubmittedEvent' callbacks from CCD",
        response = PostSubmitCallbackResponse.class,
        authorizations =
            {
                @Authorization(value = "Authorization"),
                @Authorization(value = "ServiceAuthorization")
            }
    )
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Optional confirmation text for CCD UI",
            response = PostSubmitCallbackResponse.class
        ),
        @ApiResponse(
            code = 400,
            message = "Bad Request",
            response = PostSubmitCallbackResponse.class
        ),
        @ApiResponse(
            code = 403,
            message = "Forbidden",
            response = PostSubmitCallbackResponse.class
        ),
        @ApiResponse(
            code = 415,
            message = "Unsupported Media Type",
            response = PostSubmitCallbackResponse.class
        ),
        @ApiResponse(
            code = 500,
            message = "Internal Server Error",
            response = PostSubmitCallbackResponse.class
        )
    })
    @PostMapping(path = "/ccdSubmitted")
    public ResponseEntity<PostSubmitCallbackResponse> ccdSubmitted(
        @ApiParam(value = "Asylum case data", required = true) @RequestBody Callback<CaseDataMap> callback
    ) {
        LOG.info(
            "Asylum Case CCD `ccdSubmitted` event `{}` received for Case ID `{}`",
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );

        PostSubmitCallbackResponse callbackResponse =
            callbackDispatcher.handle(callback);

        LOG.info(
            "Asylum Case CCD `ccdSubmitted` event `{}` handled for Case ID `{}`",
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );

        return ok(callbackResponse);
    }
}
