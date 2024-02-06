package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.*;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

/*
This handler handles clearing of EJP related fields for 2 scenarios:
EJP case switching from repped to unrepped and EJP switching to paper form using Edit Appeal event
 */

@Slf4j
@Component
public class EjpEditAppealHandler implements PreSubmitCallbackHandler<AsylumCase> {
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.EDIT_APPEAL)
                && isInternalCase(callback.getCaseDetails().getCaseData());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Boolean ejpCase = sourceOfAppealEjp(asylumCase);
        Boolean isLegallyRepresentedEjp = isLegallyRepresentedEjpCase(asylumCase);

        if (ejpCase) {
            // EJP case repped to unrepped scenario
            if (!isLegallyRepresentedEjp) {
                asylumCase.clear(LEGAL_REP_COMPANY_EJP);
                asylumCase.clear(LEGAL_REP_GIVEN_NAME_EJP);
                asylumCase.clear(LEGAL_REP_FAMILY_NAME_EJP);
                asylumCase.clear(LEGAL_REP_EMAIL_EJP);
                asylumCase.clear(LEGAL_REP_REFERENCE_EJP);
            }
            // Paper form to EJP scenario - clears paper form fields not in EJP flow
            asylumCase.clear(TRIBUNAL_RECEIVED_DATE);
            asylumCase.clear(HOME_OFFICE_DECISION_DATE);
            asylumCase.clear(APPELLANT_IN_UK);
            asylumCase.clear(IS_ACCELERATED_DETAINED_APPEAL);
            asylumCase.clear(DECISION_HEARING_FEE_OPTION);

            asylumCase.clear(UPLOAD_THE_APPEAL_FORM_DOCS);

        } else {
            // EJP to Paper Form scenario - clears all EJP fields
            asylumCase.clear(UPPER_TRIBUNAL_REFERENCE_NUMBER);
            asylumCase.clear(FIRST_TIER_TRIBUNAL_TRANSFER_DATE);
            asylumCase.clear(STATE_OF_THE_APPEAL);
            asylumCase.clear(UT_TRANSFER_DOC);
            asylumCase.clear(UPLOAD_EJP_APPEAL_FORM_DOCS);
            asylumCase.clear(IS_LEGALLY_REPRESENTED_EJP);
            asylumCase.clear(LEGAL_REP_COMPANY_EJP);
            asylumCase.clear(LEGAL_REP_GIVEN_NAME_EJP);
            asylumCase.clear(LEGAL_REP_FAMILY_NAME_EJP);
            asylumCase.clear(LEGAL_REP_EMAIL_EJP);
            asylumCase.clear(LEGAL_REP_REFERENCE_EJP);

            Optional<String> optionalDateReceived = asylumCase.read(DECISION_LETTER_RECEIVED_DATE, String.class);
            Optional<String> optionalDateSent = asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class);

            if (optionalDateReceived.isPresent() && optionalDateSent.isPresent()) {
                if (!isAcceleratedDetainedAppeal(asylumCase)) {
                    asylumCase.clear(DECISION_LETTER_RECEIVED_DATE);
                } else {
                    asylumCase.clear(HOME_OFFICE_DECISION_DATE);
                }
            }
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
