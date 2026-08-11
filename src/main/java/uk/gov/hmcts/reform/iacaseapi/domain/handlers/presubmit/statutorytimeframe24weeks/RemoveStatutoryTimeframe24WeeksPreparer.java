package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.statutorytimeframe24weeks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMOVAL_OF_24W_DECISION_JUDGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

@Component
public class RemoveStatutoryTimeframe24WeeksPreparer implements PreSubmitCallbackHandler<AsylumCase> {
    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;

    public RemoveStatutoryTimeframe24WeeksPreparer(UserDetails userDetails, UserDetailsHelper userDetailsHelper) {
        this.userDetails = requireNonNull(userDetails, "userDetails must not be null");
        this.userDetailsHelper = requireNonNull(userDetailsHelper, "userDetailsHelper must not be null");
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage.equals(ABOUT_TO_START) && callback.getEvent().equals(REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        asylumCase.clear(AsylumCaseFieldDefinition.REMOVAL_OF_24W_DECISION_REASON);

        UserRoleLabel userRole = userDetailsHelper.getLoggedInUserRoleLabel(userDetails);
        if (UserRoleLabel.JUDGE.equals(userRole)) {
            asylumCase.write(REMOVAL_OF_24W_DECISION_JUDGE, userDetails.getForenameAndSurname());
        } else {
            asylumCase.clear(REMOVAL_OF_24W_DECISION_JUDGE);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
