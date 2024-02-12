package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.sourceOfAppealEjp;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class EjpContactPreferenceFieldsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && (callback.getEvent() == Event.START_APPEAL || callback.getEvent() == Event.EDIT_APPEAL)
                && sourceOfAppealEjp(callback.getCaseDetails().getCaseData()));
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        if (asylumCase.read(IS_LEGALLY_REPRESENTED_EJP, YesOrNo.class).orElse(NO).equals(NO)) {
            String emailUnRep = asylumCase.read(EMAIL_UNREP, String.class).orElse("");
            String mobileNumberUnRep = asylumCase.read(MOBILE_NUMBER_UNREP, String.class).orElse("");

            asylumCase.write(EMAIL, emailUnRep);
            asylumCase.write(MOBILE_NUMBER, mobileNumberUnRep);
        }

        if (callback.getEvent() == Event.EDIT_APPEAL) {
            if (asylumCase.read(IS_LEGALLY_REPRESENTED_EJP, YesOrNo.class).orElse(NO).equals(YES)) {

                asylumCase.clear(EMAIL_UNREP);
                asylumCase.clear(MOBILE_NUMBER_UNREP);
                asylumCase.clear(CONTACT_PREFERENCE_UNREP);

                asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class).ifPresent(
                    contactPreference -> {
                        if (contactPreference.equals(ContactPreference.WANTS_EMAIL)) {
                            asylumCase.clear(MOBILE_NUMBER);
                        } else {
                            asylumCase.clear(EMAIL);
                        }
                    }
                );

            } else {
                asylumCase.clear(CONTACT_PREFERENCE);
            }
        }

        return response;
    }
}
