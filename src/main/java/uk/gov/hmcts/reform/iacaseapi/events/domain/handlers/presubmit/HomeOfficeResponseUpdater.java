package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.presubmit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.events.domain.service.DocumentAppender;
import uk.gov.hmcts.reform.iacaseapi.events.domain.service.SentDirectionCompleter;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.Documents;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.HomeOfficeResponse;

@Component
public class HomeOfficeResponseUpdater implements PreSubmitCallbackHandler<AsylumCase> {

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
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.ABOUT_TO_SUBMIT
               && callback.getEventId() == EventId.ADD_HOME_OFFICE_RESPONSE;
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
