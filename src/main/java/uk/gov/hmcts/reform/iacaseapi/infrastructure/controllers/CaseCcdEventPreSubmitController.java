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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPreSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitDispatcher;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization.Deserializer;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization.Serializer;

@RestController
@RequestMapping(
    path = "/asylum",
    consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
public class CaseCcdEventPreSubmitController {

    private static final org.slf4j.Logger LOG = getLogger(CaseCcdEventPreSubmitController.class);

    private Deserializer<CcdEvent<AsylumCase>> ccdEventDeserializer;
    private CcdEventPreSubmitDispatcher<AsylumCase> preSubmitDispatcher;
    private Serializer<CcdEventPreSubmitResponse<AsylumCase>> preSubmitResponseSerializer;

    public CaseCcdEventPreSubmitController(
        @Autowired Deserializer<CcdEvent<AsylumCase>> ccdEventDeserializer,
        @Autowired CcdEventPreSubmitDispatcher<AsylumCase> preSubmitDispatcher,
        @Autowired Serializer<CcdEventPreSubmitResponse<AsylumCase>> preSubmitResponseSerializer
    ) {
        this.ccdEventDeserializer = ccdEventDeserializer;
        this.preSubmitDispatcher = preSubmitDispatcher;
        this.preSubmitResponseSerializer = preSubmitResponseSerializer;
    }

    @PostMapping(path = "/ccdAboutToStart")
    public ResponseEntity<CcdEventPreSubmitResponse<AsylumCase>> ccdAboutToStart(
        @RequestBody String source
    ) {
        return performStageRequest(Stage.ABOUT_TO_START, source);
    }

    @PostMapping(path = "/ccdAboutToSubmit")
    public ResponseEntity<CcdEventPreSubmitResponse<AsylumCase>> ccdAboutToSubmit(
        @RequestBody String source
    ) {
        return performStageRequest(Stage.ABOUT_TO_SUBMIT, source);
    }

    private ResponseEntity<CcdEventPreSubmitResponse<AsylumCase>> performStageRequest(
        Stage stage,
        String source
    ) {
        LOG.info("Asylum Case CCD `{}` event source: {}", stage, source);

        CcdEvent<AsylumCase> ccdEvent =
            ccdEventDeserializer.deserialize(source);

        CcdEventPreSubmitResponse<AsylumCase> preSubmitResponse =
            preSubmitDispatcher.handle(stage, ccdEvent);

        LOG.info(
            "Asylum Case CCD `{}` event response: {}",
            stage,
            preSubmitResponseSerializer
                .serialize(preSubmitResponse)
        );

        return ok(preSubmitResponse);
    }
}
