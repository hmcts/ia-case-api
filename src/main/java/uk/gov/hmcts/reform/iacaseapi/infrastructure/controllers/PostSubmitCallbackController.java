package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization.Deserializer;

@Api(
    value = "Handles callbacks from CCD that occur *before* changes are persisted.",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequestMapping(
    path = "/asylum",
    consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
@RestController
public class PostSubmitCallbackController {

    private static final org.slf4j.Logger LOG = getLogger(PostSubmitCallbackController.class);

    private final Deserializer<Callback<AsylumCase>> callbackDeserializer;
    private final PostSubmitCallbackDispatcher<AsylumCase> callbackDispatcher;

    public PostSubmitCallbackController(
        Deserializer<Callback<AsylumCase>> callbackDeserializer,
        PostSubmitCallbackDispatcher<AsylumCase> callbackDispatcher
    ) {
        requireNonNull(callbackDeserializer, "callbackDeserializer must not be null");
        requireNonNull(callbackDispatcher, "callbackDispatcher must not be null");

        this.callbackDeserializer = callbackDeserializer;
        this.callbackDispatcher = callbackDispatcher;
    }

    @ApiOperation("Handles 'SubmittedEvent' callbacks from CCD")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Optional confirmation text for CCD UI",
            response = PostSubmitCallbackResponse.class
        )
    })
    @PostMapping(path = "/ccdSubmitted")
    public ResponseEntity<PostSubmitCallbackResponse> ccdSubmitted(
        @RequestBody String source
    ) {
        Callback<AsylumCase> callback =
            callbackDeserializer.deserialize(source);

        LOG.info(
            "Asylum Case CCD `submitted` event received for Case ID `{}`",
            callback.getCaseDetails().getId()
        );

        PostSubmitCallbackResponse callbackResponse =
            callbackDispatcher.handle(callback);

        LOG.info(
            "Asylum Case CCD `submitted` event handled for Case ID `{}`",
            callback.getCaseDetails().getId()
        );

        return ok(callbackResponse);
    }
}
