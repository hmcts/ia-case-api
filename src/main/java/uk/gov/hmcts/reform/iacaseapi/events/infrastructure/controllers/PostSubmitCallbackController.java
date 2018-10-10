package uk.gov.hmcts.reform.iacaseapi.events.infrastructure.controllers;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.ResponseEntity.ok;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PostSubmitCallbackDispatcher;
import uk.gov.hmcts.reform.iacaseapi.shared.infrastructure.serialization.Deserializer;
import uk.gov.hmcts.reform.iacaseapi.shared.infrastructure.serialization.Serializer;

@RestController
@RequestMapping(
    path = "/asylum",
    consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
public class PostSubmitCallbackController {

    private static final org.slf4j.Logger LOG = getLogger(PostSubmitCallbackController.class);

    private Deserializer<Callback<AsylumCase>> callbackDeserializer;
    private PostSubmitCallbackDispatcher<AsylumCase> postSubmitDispatcher;
    private Serializer<PostSubmitCallbackResponse> postSubmitResponseSerializer;

    public PostSubmitCallbackController(
        @Autowired Deserializer<Callback<AsylumCase>> callbackDeserializer,
        @Autowired PostSubmitCallbackDispatcher<AsylumCase> postSubmitDispatcher,
        @Autowired Serializer<PostSubmitCallbackResponse> postSubmitResponseSerializer
    ) {
        this.callbackDeserializer = callbackDeserializer;
        this.postSubmitDispatcher = postSubmitDispatcher;
        this.postSubmitResponseSerializer = postSubmitResponseSerializer;
    }

    @PostMapping(path = "/ccdSubmitted")
    public ResponseEntity<PostSubmitCallbackResponse> ccdSubmitted(
        @RequestBody String source
    ) {
        LOG.info("Asylum Case CCD `{}` event source: {}", CallbackStage.SUBMITTED, source);

        Callback<AsylumCase> callback =
            callbackDeserializer.deserialize(source);

        PostSubmitCallbackResponse postSubmitResponse =
            postSubmitDispatcher.handle(CallbackStage.SUBMITTED, callback);

        LOG.info(
            "Asylum Case CCD `{}` event response: {}",
            CallbackStage.SUBMITTED,
            postSubmitResponseSerializer
                .serialize(postSubmitResponse)
        );

        return ok(postSubmitResponse);
    }
}
