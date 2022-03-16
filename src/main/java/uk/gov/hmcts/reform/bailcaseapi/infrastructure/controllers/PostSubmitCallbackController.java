package uk.gov.hmcts.reform.bailcaseapi.infrastructure.controllers;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.PostSubmitCallbackDispatcher;

@Slf4j
@Api(
    value = "/bail",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequestMapping(
    value = "/bail",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RestController
public class PostSubmitCallbackController {

    private final PostSubmitCallbackDispatcher<BailCase> callbackDispatcher;

    public PostSubmitCallbackController(
        PostSubmitCallbackDispatcher<BailCase> callbackDispatcher
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
            code = 500,
            message = "Internal Server Error",
            response = PostSubmitCallbackResponse.class
        )
    })
    @PostMapping(path = "/ccdSubmitted")
    public ResponseEntity<PostSubmitCallbackResponse> ccdSubmitted(
        @ApiParam(value = "Bail case data", required = true) @RequestBody Callback<BailCase> callback
    ) {
        log.info(
            "Bail Case CCD `ccdSubmitted` event `{}` received for Case ID `{}`",
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );

        PostSubmitCallbackResponse callbackResponse =
            callbackDispatcher.handle(callback);

        log.info(
            "Bail Case CCD `ccdSubmitted` event `{}` handled for Case ID `{}`",
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );
        return ok(callbackResponse);
    }

}
