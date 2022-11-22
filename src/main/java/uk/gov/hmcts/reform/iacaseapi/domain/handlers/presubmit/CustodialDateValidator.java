package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CustodialSentenceDate;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class CustodialDateValidator implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String CUSTODIAL_SENTENCE_PAGE_ID = "custodialSentence";

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && callback.getPageId().equals(CUSTODIAL_SENTENCE_PAGE_ID)
               && Arrays.asList(Event.START_APPEAL, Event.EDIT_APPEAL, Event.EDIT_APPEAL_AFTER_SUBMIT).contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        Optional<CustodialSentenceDate> optionalCustodialSentenceDate = asylumCase
            .read(DATE_CUSTODIAL_SENTENCE, CustodialSentenceDate.class);

        optionalCustodialSentenceDate.ifPresent(custodialSentenceDate -> {
            LocalDate custodialDate = LocalDate.parse(custodialSentenceDate.getCustodialDate());
            if (!custodialDate.isAfter(LocalDate.now())) {
                response.addError("Client's release date must be in the future");
            }
        });

        return response;
    }

}
