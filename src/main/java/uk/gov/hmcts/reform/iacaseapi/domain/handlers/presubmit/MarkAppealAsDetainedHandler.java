package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionFacility.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.MARK_APPEAL_AS_DETAINED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CustodialSentenceDate;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PrisonNomsNumber;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class MarkAppealAsDetainedHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == MARK_APPEAL_AS_DETAINED;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        asylumCase.read(PRISON_NOMS_AO, PrisonNomsNumber.class)
            .ifPresent(prisonNomsAo -> asylumCase.write(PRISON_NOMS, prisonNomsAo));

        asylumCase.read(DATE_CUSTODIAL_SENTENCE_AO, CustodialSentenceDate.class)
            .ifPresent(dateAo -> asylumCase.write(DATE_CUSTODIAL_SENTENCE, dateAo));

        // clearing non-detention related fields
        String detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class)
            .orElseThrow(() -> new IllegalStateException("detentionFacility missing on when marking as detained"));

        if (!detentionFacility.equals(OTHER.getValue())) {
            // We use this to store "Other" detention facility address
            asylumCase.clear(APPELLANT_ADDRESS);
        }
        asylumCase.clear(APPELLANT_HAS_FIXED_ADDRESS);
        asylumCase.clear(CONTACT_PREFERENCE);
        asylumCase.clear(EMAIL);
        asylumCase.clear(MOBILE_NUMBER);
        asylumCase.clear(DETENTION_REMOVAL_REASON);
        asylumCase.clear(DETENTION_REMOVAL_DATE);
        asylumCase.clear(JOURNEY_TYPE);

        asylumCase.write(APPELLANT_IN_DETENTION, YES);
        asylumCase.write(IS_ADMIN, YES);

        return new PreSubmitCallbackResponse<>(asylumCase);

    }

}
