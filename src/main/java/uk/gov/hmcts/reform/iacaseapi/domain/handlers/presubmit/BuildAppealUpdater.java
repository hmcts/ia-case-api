package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Documents;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.LegalArgument;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentAppender;

@Component
public class BuildAppealUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    private final DocumentAppender documentAppender;

    public BuildAppealUpdater(
        @Autowired DocumentAppender documentAppender
    ) {
        this.documentAppender = documentAppender;
    }

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && ccdEvent.getEventId() == EventId.BUILD_APPEAL;
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

        Document legalArgumentDocument =
            asylumCase
                .getLegalArgumentDocument()
                .orElseThrow(() -> new IllegalStateException("legalArgumentDocument not present"));

        String legalArgumentDescription =
            asylumCase
                .getLegalArgumentDescription()
                .orElseThrow(() -> new IllegalStateException("legalArgumentDescription not present"));

        Documents legalArgumentEvidence =
            asylumCase
                .getLegalArgumentEvidence()
                .orElseThrow(() -> new IllegalStateException("legalArgumentEvidence not present"));

        asylumCase
            .getCaseArgument()
            .orElseThrow(() -> new IllegalStateException("caseArgument not present"))
            .setLegalArgument(
                new LegalArgument(
                    legalArgumentDocument,
                    legalArgumentDescription,
                    legalArgumentEvidence
                )
            );

        documentAppender.append(asylumCase, legalArgumentDocument);

        asylumCase
            .getLegalArgumentEvidence()
            .get()
            .getDocuments()
            .orElse(Collections.emptyList())
            .stream()
            .map(IdValue::getValue)
            .forEach(document -> documentAppender.append(asylumCase, document));

        return preSubmitResponse;
    }
}
