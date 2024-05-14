package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CustodialSentenceDate;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
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
               && Arrays.asList(Event.START_APPEAL,
            Event.EDIT_APPEAL,
            Event.EDIT_APPEAL_AFTER_SUBMIT,
            Event.MARK_APPEAL_AS_DETAINED,
            Event.UPDATE_DETENTION_LOCATION).contains(callback.getEvent());
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

        YesOrNo clientInCustody = asylumCase.read(CUSTODIAL_SENTENCE, YesOrNo.class).orElse(YesOrNo.NO);
        if (clientInCustody.equals(YesOrNo.NO)) {
            return response;
        }

        AsylumCaseFieldDefinition fieldToBeChecked;
        String appellantQualifier;

        if (callback.getEvent().equals(Event.MARK_APPEAL_AS_DETAINED)) {
            fieldToBeChecked = DATE_CUSTODIAL_SENTENCE_AO;
            appellantQualifier = "Appellant";
        } else {
            fieldToBeChecked = DATE_CUSTODIAL_SENTENCE;
            appellantQualifier = "Client";
        }

        CustodialSentenceDate custodialSentence = asylumCase.read(fieldToBeChecked, CustodialSentenceDate.class)
                .orElseThrow(() -> new RequiredFieldMissingException("custodialSentence value indicates " + fieldToBeChecked + " present, but not found"));

        if (custodialSentence.getCustodialDate() == null) {
            return response;
        }

        LocalDate custodialDate = LocalDate.parse(custodialSentence.getCustodialDate());
        if (!custodialDate.isAfter(LocalDate.now())) {
            response.addError(appellantQualifier + "'s release date must be in the future");
        }

        return response;
    }

}
