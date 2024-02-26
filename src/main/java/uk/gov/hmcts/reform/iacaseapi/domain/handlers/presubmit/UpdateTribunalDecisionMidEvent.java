package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealDecision.ALLOWED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealDecision.DISMISSED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.ArrayList;
import java.util.List;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class UpdateTribunalDecisionMidEvent implements PreSubmitCallbackHandler<AsylumCase> {

    private final UserDetailsHelper userDetailsHelper;
    private final UserDetails userDetails;
    static final String ROLE_JUDGE = "caseworker-ia-iacjudge";

    public UpdateTribunalDecisionMidEvent(UserDetailsHelper userDetailsHelper, UserDetails userDetails) {
        this.userDetailsHelper = userDetailsHelper;
        this.userDetails = userDetails;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && callback.getEvent() == Event.UPDATE_TRIBUNAL_DECISION
               && callback.getPageId().equals("tribunalDecisionType");
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        boolean isJudge = ROLE_JUDGE.contains(userDetailsHelper.getLoggedInUserRole(userDetails).toString());
        boolean isDecisionAllowed = isDecisionAllowed(asylumCase);

        final List<Value> values = new ArrayList<>();
        if (isJudge && isDecisionAllowed) {
            values.add(new Value(ALLOWED.name(), "Yes, change decision to Dismissed"));
            values.add(new Value(DISMISSED.name(), "No"));
        } else if (isJudge) {
            values.add(new Value(ALLOWED.name(), "Yes, change decision to Allowed"));
            values.add(new Value(DISMISSED.name(), "No"));
        }


        DynamicList typesOfUpdateTribunalDecision = new DynamicList(new Value("", ""), values);

        asylumCase.write(TYPES_OF_UPDATE_TRIBUNAL_DECISION, typesOfUpdateTribunalDecision);
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isDecisionAllowed(AsylumCase asylumCase) {
        return asylumCase
            .read(IS_DECISION_ALLOWED, AppealDecision.class)
            .map(type -> type == ALLOWED).orElse(false);
    }
}
