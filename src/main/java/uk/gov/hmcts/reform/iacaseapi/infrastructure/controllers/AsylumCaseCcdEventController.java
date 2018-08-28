package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.ResponseEntity.ok;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.AsylumCaseCcdEventHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization.Deserializer;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization.Serializer;

@RestController
@RequestMapping(
    path = "/asylum",
    consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
public class AsylumCaseCcdEventController {

    private static final org.slf4j.Logger LOG = getLogger(AsylumCaseCcdEventController.class);

    private Deserializer<CcdEvent<AsylumCase>> asylumCaseCcdEventDeserializer;
    private AsylumCaseCcdEventHandler asylumCaseCcdEventHandler;
    private Serializer<CcdEventResponse<AsylumCase>> asylumCaseCcdEventResponseSerializer;

    public AsylumCaseCcdEventController(
        @Autowired Deserializer<CcdEvent<AsylumCase>> asylumCaseCcdEventDeserializer,
        @Autowired AsylumCaseCcdEventHandler asylumCaseCcdEventHandler,
        @Autowired Serializer<CcdEventResponse<AsylumCase>> asylumCaseCcdEventResponseSerializer
    ) {
        this.asylumCaseCcdEventDeserializer = asylumCaseCcdEventDeserializer;
        this.asylumCaseCcdEventHandler = asylumCaseCcdEventHandler;
        this.asylumCaseCcdEventResponseSerializer = asylumCaseCcdEventResponseSerializer;
    }

    @PostMapping(path = "/ccdAboutToStart")
    public ResponseEntity<CcdEventResponse<AsylumCase>> ccdAboutToStart(
        @RequestBody String source
    ) {
        return performStageRequest(Stage.ABOUT_TO_START, source);
    }

    @PostMapping(path = "/ccdAboutToSubmit")
    public ResponseEntity<CcdEventResponse<AsylumCase>> ccdAboutToSubmit(
        @RequestBody String source
    ) {
        return performStageRequest(Stage.ABOUT_TO_SUBMIT, source);
    }

    @PostMapping(path = "/ccdSubmitted")
    public ResponseEntity<CcdEventResponse<AsylumCase>> ccdSubmitted(
        @RequestBody String source
    ) {
        return performStageRequest(Stage.SUBMITTED, source);
    }

    private ResponseEntity<CcdEventResponse<AsylumCase>> performStageRequest(
        Stage stage,
        String source
    ) {
        LOG.info("Asylum Case CCD `{}` event source: {}", stage, source);

        CcdEvent<AsylumCase> ccdEvent =
            asylumCaseCcdEventDeserializer.deserialize(source);

        CcdEventResponse<AsylumCase> ccdEventResponse =
            asylumCaseCcdEventHandler.handle(stage, ccdEvent);

        LOG.info(
            "Asylum Case CCD `{}` event response: {}",
            stage,
            asylumCaseCcdEventResponseSerializer
                .serialize(ccdEventResponse)
        );

        return ok(ccdEventResponse);
    }
}
