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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPostSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPostSubmitDispatcher;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization.Deserializer;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization.Serializer;

@RestController
@RequestMapping(
    path = "/asylum",
    consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
public class CaseCcdEventPostSubmitController {

    private static final org.slf4j.Logger LOG = getLogger(CaseCcdEventPostSubmitController.class);

    private Deserializer<CcdEvent<AsylumCase>> ccdEventDeserializer;
    private CcdEventPostSubmitDispatcher<AsylumCase> postSubmitDispatcher;
    private Serializer<CcdEventPostSubmitResponse> postSubmitResponseSerializer;

    public CaseCcdEventPostSubmitController(
        @Autowired Deserializer<CcdEvent<AsylumCase>> ccdEventDeserializer,
        @Autowired CcdEventPostSubmitDispatcher<AsylumCase> postSubmitDispatcher,
        @Autowired Serializer<CcdEventPostSubmitResponse> postSubmitResponseSerializer
    ) {
        this.ccdEventDeserializer = ccdEventDeserializer;
        this.postSubmitDispatcher = postSubmitDispatcher;
        this.postSubmitResponseSerializer = postSubmitResponseSerializer;
    }

    @PostMapping(path = "/ccdSubmitted")
    public ResponseEntity<CcdEventPostSubmitResponse> ccdSubmitted(
        @RequestBody String source
    ) {
        LOG.info("Asylum Case CCD `{}` event source: {}", Stage.SUBMITTED, source);

        CcdEvent<AsylumCase> ccdEvent =
            ccdEventDeserializer.deserialize(source);

        CcdEventPostSubmitResponse postSubmitResponse =
            postSubmitDispatcher.handle(Stage.SUBMITTED, ccdEvent);

        LOG.info(
            "Asylum Case CCD `{}` event response: {}",
            Stage.SUBMITTED,
            postSubmitResponseSerializer
                .serialize(postSubmitResponse)
        );

        return ok(postSubmitResponse);
    }
}
