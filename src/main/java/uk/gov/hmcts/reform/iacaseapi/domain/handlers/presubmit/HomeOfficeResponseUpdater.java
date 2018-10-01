package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Documents;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HomeOfficeResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.SentDirectionCompleter;

@Component
public class HomeOfficeResponseUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    private final DocumentAppender documentAppender;
    private final SentDirectionCompleter sentDirectionCompleter;

    public HomeOfficeResponseUpdater(
        @Autowired DocumentAppender documentAppender,
        @Autowired SentDirectionCompleter sentDirectionCompleter
    ) {
        this.documentAppender = documentAppender;
        this.sentDirectionCompleter = sentDirectionCompleter;
    }

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && ccdEvent.getEventId() == EventId.ADD_HOME_OFFICE_RESPONSE;
    }

    public CcdEventPreSubmitResponse<AsylumCase> handle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        if (!canHandle(stage, ccdEvent)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            ccdEvent
                .getCaseDetails()
                .getCaseData();

        CcdEventPreSubmitResponse<AsylumCase> preSubmitResponse =
            new CcdEventPreSubmitResponse<>(asylumCase);

        Document homeOfficeResponseDocument =
            asylumCase
                .getHomeOfficeResponseDocument()
                .orElseThrow(() -> new IllegalStateException("homeOfficeResponseDocument not present"));

        String homeOfficeResponseDescription =
            asylumCase
                .getHomeOfficeResponseDescription()
                .orElseThrow(() -> new IllegalStateException("homeOfficeResponseDescription not present"));

        Documents homeOfficeResponseEvidence =
            asylumCase
                .getHomeOfficeResponseEvidence()
                .orElseThrow(() -> new IllegalStateException("homeOfficeResponseEvidence not present"));

        String homeOfficeResponseDate =
            asylumCase
                .getHomeOfficeResponseDate()
                .orElseThrow(() -> new IllegalStateException("homeOfficeResponseDate not present"));

        asylumCase
            .getCaseArgument()
            .orElseThrow(() -> new IllegalStateException("caseArgument not present"))
            .setHomeOfficeResponse(
                new HomeOfficeResponse(
                    homeOfficeResponseDocument,
                    homeOfficeResponseDescription,
                    homeOfficeResponseEvidence,
                    homeOfficeResponseDate
                )
            );

        documentAppender.append(asylumCase, homeOfficeResponseDocument, homeOfficeResponseDescription);

        sentDirectionCompleter.tryMarkAsComplete(asylumCase, "homeOfficeReview");

        asylumCase.clearHomeOfficeResponse();

        return preSubmitResponse;
    }
}
