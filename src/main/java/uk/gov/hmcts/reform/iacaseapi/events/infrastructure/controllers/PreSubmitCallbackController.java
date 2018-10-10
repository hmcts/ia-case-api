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
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PreSubmitCallbackDispatcher;
import uk.gov.hmcts.reform.iacaseapi.shared.infrastructure.serialization.Deserializer;
import uk.gov.hmcts.reform.iacaseapi.shared.infrastructure.serialization.Serializer;

@RestController
@RequestMapping(
    path = "/asylum",
    consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
public class PreSubmitCallbackController {

    private static final org.slf4j.Logger LOG = getLogger(PreSubmitCallbackController.class);

    private Deserializer<Callback<AsylumCase>> callbackDeserializer;
    private PreSubmitCallbackDispatcher<AsylumCase> preSubmitDispatcher;
    private Serializer<PreSubmitCallbackResponse<AsylumCase>> preSubmitResponseSerializer;

    public PreSubmitCallbackController(
        @Autowired Deserializer<Callback<AsylumCase>> callbackDeserializer,
        @Autowired PreSubmitCallbackDispatcher<AsylumCase> preSubmitDispatcher,
        @Autowired Serializer<PreSubmitCallbackResponse<AsylumCase>> preSubmitResponseSerializer
    ) {
        this.callbackDeserializer = callbackDeserializer;
        this.preSubmitDispatcher = preSubmitDispatcher;
        this.preSubmitResponseSerializer = preSubmitResponseSerializer;
    }

    @PostMapping(path = "/ccdAboutToStart")
    public ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> ccdAboutToStart(
        @RequestBody String source
    ) {
        return performStageRequest(CallbackStage.ABOUT_TO_START, source);
    }

    @PostMapping(path = "/ccdAboutToSubmit")
    public ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> ccdAboutToSubmit(
        @RequestBody String source
    ) {
        return performStageRequest(CallbackStage.ABOUT_TO_SUBMIT, source);
    }

    private ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> performStageRequest(
        CallbackStage callbackStage,
        String source
    ) {
        LOG.info("Asylum Case CCD `{}` event source: {}", callbackStage, source);

        Callback<AsylumCase> callback =
            callbackDeserializer.deserialize(source);

        PreSubmitCallbackResponse<AsylumCase> preSubmitResponse =
            preSubmitDispatcher.handle(callbackStage, callback);

        LOG.info(
            "Asylum Case CCD `{}` event response: {}",
            callbackStage,
            preSubmitResponseSerializer
                .serialize(preSubmitResponse)
        );

        return ok(preSubmitResponse);
    }
}
