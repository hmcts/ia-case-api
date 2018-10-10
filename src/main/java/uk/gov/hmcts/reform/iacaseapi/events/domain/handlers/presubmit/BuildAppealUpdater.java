package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.presubmit;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.events.domain.service.DocumentAppender;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.Documents;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.LegalArgument;

@Component
public class BuildAppealUpdater implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentAppender documentAppender;

    public BuildAppealUpdater(
        @Autowired DocumentAppender documentAppender
    ) {
        this.documentAppender = documentAppender;
    }

    public boolean canHandle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.ABOUT_TO_SUBMIT
               && callback.getEventId() == EventId.BUILD_APPEAL;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<AsylumCase> preSubmitResponse =
            new PreSubmitCallbackResponse<>(asylumCase);

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
