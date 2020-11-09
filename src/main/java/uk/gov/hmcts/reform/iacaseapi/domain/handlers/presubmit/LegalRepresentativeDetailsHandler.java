package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class LegalRepresentativeDetailsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final UserDetailsProvider userDetailsProvider;

    public LegalRepresentativeDetailsHandler(
            UserDetailsProvider userDetailsProvider
    ) {
        this.userDetailsProvider = userDetailsProvider;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLIEST;
    }

    @Override
    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && Arrays.asList(
                    Event.SUBMIT_APPEAL,
                    Event.PAY_AND_SUBMIT_APPEAL)
                   .contains(callback.getEvent());
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        UserDetails userDetails = userDetailsProvider.getUserDetails();

        final AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        if (asylumCase.read(LEGAL_REPRESENTATIVE_NAME).isEmpty()) {
            asylumCase.write(
                    LEGAL_REPRESENTATIVE_NAME,
                    userDetails.getForename() + " " + userDetails.getSurname()
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

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
