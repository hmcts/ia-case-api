package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLIED_COSTS_TYPES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.TypesOfAppliedCosts.UNREASONABLE_COSTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.TypesOfAppliedCosts.WASTED_COSTS;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ApplyForCostsMidEvent implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String ROLE_LEGAL_REP = "caseworker-ia-legalrep-solicitor";
    private static final String ROLE_HO_APC = "caseworker-ia-homeofficeapc";
    private static final String ROLE_HO_LART = "caseworker-ia-homeofficelart";
    private static final String ROLE_HO_POU = "caseworker-ia-homeofficepou";
    private static final List<String> LEGAL_REP_OR_HO_ROLES = List.of(ROLE_LEGAL_REP, ROLE_HO_APC, ROLE_HO_LART, ROLE_HO_POU);

    private final UserDetailsHelper userDetailsHelper;
    private final UserDetails userDetails;

    public ApplyForCostsMidEvent(UserDetailsHelper userDetailsHelper, UserDetails userDetails) {
        this.userDetailsHelper = userDetailsHelper;
        this.userDetails = userDetails;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && callback.getEvent() == Event.APPLY_FOR_COSTS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        boolean isLegalRepOrHomeOffice = LEGAL_REP_OR_HO_ROLES.contains(userDetailsHelper.getLoggedInUserRole(userDetails).toString());

        final List<Value> values = new ArrayList<>();
        if (isLegalRepOrHomeOffice) {
            values.add(new Value(UNREASONABLE_COSTS.name(), UNREASONABLE_COSTS.toString()));
            values.add(new Value(WASTED_COSTS.name(), WASTED_COSTS.toString()));
        }

        DynamicList typesOfAppliedCosts = new DynamicList(new Value("", ""), values);

        asylumCase.write(APPLIED_COSTS_TYPES, typesOfAppliedCosts);
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
