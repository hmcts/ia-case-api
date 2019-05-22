package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumExtractor.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumExtractor.LEGAL_REPRESENTATIVE_NAME;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class LegalRepresentativeDetailsHandler implements PreSubmitCallbackHandler<CaseDataMap> {

    private final UserDetailsProvider userDetailsProvider;

    public LegalRepresentativeDetailsHandler(
            UserDetailsProvider userDetailsProvider
    ) {
        this.userDetailsProvider = userDetailsProvider;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<CaseDataMap> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.SUBMIT_APPEAL;
    }

    public PreSubmitCallbackResponse<CaseDataMap> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<CaseDataMap> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        UserDetails userDetails = userDetailsProvider.getUserDetails();

        final CaseDataMap caseDataMap =
                callback
                        .getCaseDetails()
                        .getCaseData();

        if (!caseDataMap.get(LEGAL_REPRESENTATIVE_NAME).isPresent()) {
            caseDataMap.write(
                    LEGAL_REPRESENTATIVE_NAME,
                    userDetails.getForename() + " " + userDetails.getSurname()
            );
        }

        if (!caseDataMap.get(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS).isPresent()) {
            caseDataMap.write(
                    LEGAL_REPRESENTATIVE_EMAIL_ADDRESS,
                    userDetails.getEmailAddress()
            );
        }

        return new PreSubmitCallbackResponse<>(caseDataMap);
    }
}
