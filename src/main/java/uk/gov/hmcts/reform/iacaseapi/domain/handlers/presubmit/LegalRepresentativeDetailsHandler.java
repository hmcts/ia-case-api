package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Arrays;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class LegalRepresentativeDetailsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final UserDetails userDetails;

    public LegalRepresentativeDetailsHandler(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && Arrays.asList(
                    Event.SUBMIT_APPEAL)
                   .contains(callback.getEvent())
                && HandlerUtils.isRepJourney(callback.getCaseDetails().getCaseData());
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

        YesOrNo isAdmin = asylumCase.read(IS_ADMIN, YesOrNo.class).orElse(YesOrNo.NO);

        if (isAdmin.equals(YesOrNo.NO)) {
            if (asylumCase.read(LEGAL_REPRESENTATIVE_NAME).isEmpty()) {
                asylumCase.write(
                        LEGAL_REPRESENTATIVE_NAME,
                        userDetails.getForename() + " " + userDetails.getSurname()
                );
            }

            if (asylumCase.read(LEGAL_REP_MOBILE_PHONE_NUMBER).isEmpty()) {
                asylumCase.write(
                        LEGAL_REP_MOBILE_PHONE_NUMBER,
                        asylumCase.read(LEGAL_REP_MOBILE_PHONE_NUMBER, String.class).orElse("")
                );
            }


            if (asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS).isEmpty()) {
                asylumCase.write(
                        LEGAL_REPRESENTATIVE_EMAIL_ADDRESS,
                        userDetails.getEmailAddress()
                );
            }


            if (asylumCase.read(LEGAL_REP_COMPANY).isEmpty()) {
                asylumCase.write(
                        LEGAL_REP_COMPANY,
                        asylumCase.read(LEGAL_REP_COMPANY, String.class).orElse("")
                );
            }

            if (asylumCase.read(LEGAL_REP_NAME).isEmpty()) {
                asylumCase.write(
                        LEGAL_REP_NAME,
                        asylumCase.read(LEGAL_REP_NAME, String.class).orElse("")
                );
            }

            asylumCase.write(HAS_ADDED_LEGAL_REP_DETAILS, YES);
        }

        if (asylumCase.read(LEGAL_REP_FAMILY_NAME).isEmpty()) {
            asylumCase.write(
                    LEGAL_REP_FAMILY_NAME,
                    asylumCase.read(LEGAL_REP_FAMILY_NAME, String.class).orElse("")
            );
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
