package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADA_HEARING_REQUIREMENTS_TO_REVIEW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.*;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@Service
public class MakeAnApplicationTypesProvider {

    private static final String ROLE_LEGAL_REP = "caseworker-ia-legalrep-solicitor";
    private static final String ROLE_HO_RESPONDENT = "caseworker-ia-respondentofficer";

    private final UserDetails userDetails;

    public MakeAnApplicationTypesProvider(
        UserDetails userDetails
    ) {
        this.userDetails = userDetails;
    }

    public DynamicList getMakeAnApplicationTypes(Callback<AsylumCase> callback) {

        final State currentState = callback.getCaseDetails().getState();

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        boolean hasHomeOfficeRole = userDetails.isHomeOffice();
        boolean isInternalAndAdminRole = userDetails.isAdmin() && isInternalCase(asylumCase);

        final List<Value> values = new ArrayList<>();
        switch (currentState) {
            case APPEAL_SUBMITTED:
                addJudgeReview(values, hasPrivilegedRole(hasHomeOfficeRole, isInternalAndAdminRole, hasRole(ROLE_LEGAL_REP)));
                addIf(hasRole(ROLE_LEGAL_REP), values, UPDATE_APPEAL_DETAILS);
                addIf(isAcceleratedDetainedAppeal(asylumCase) && hasRole(ROLE_LEGAL_REP), values, TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS);
                addIf(isAcceleratedDetainedAppeal(asylumCase) && isInternalAndAdminRole, values,
                        TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS, UPDATE_APPEAL_DETAILS);
                addCommonEndStates(values);
                addType(values, CHANGE_DECISION_TYPE);
                break;

            case ENDED:
                addJudgeReview(values, hasPrivilegedRole(hasHomeOfficeRole, isInternalAndAdminRole, hasRole(ROLE_LEGAL_REP)));
                addType(values, REINSTATE);
                break;

            case PENDING_PAYMENT:
            case AWAITING_RESPONDENT_EVIDENCE:
            case CASE_BUILDING:
            case AWAITING_REASONS_FOR_APPEAL:
            case AWAITING_CLARIFYING_QUESTIONS_ANSWERS:
            case AWAITING_CMA_REQUIREMENTS:
            case CASE_UNDER_REVIEW:
            case REASONS_FOR_APPEAL_SUBMITTED:
            case RESPONDENT_REVIEW:
            case SUBMIT_HEARING_REQUIREMENTS:
                addType(values, TIME_EXTENSION);

                if (isInternalAndAdminRole && isAcceleratedDetainedAppeal(asylumCase)) {
                    addType(values, ADJOURN, EXPEDITE);
                    if (hasSubmittedHearingRequirements(asylumCase)) {
                        addType(values, UPDATE_HEARING_REQUIREMENTS);
                    }
                    if (currentState != PENDING_PAYMENT) {
                        addType(values, TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS, UPDATE_APPEAL_DETAILS);
                    }
                }

                addJudgeReview(values, hasPrivilegedRole(hasHomeOfficeRole, isInternalAndAdminRole, hasRole(ROLE_LEGAL_REP)));

                if (hasRole(ROLE_LEGAL_REP)) {
                    addType(values, UPDATE_APPEAL_DETAILS);
                    if (isAcceleratedDetainedAppeal(asylumCase) && currentState != PENDING_PAYMENT) {
                        addType(values, ADJOURN, EXPEDITE, TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS, UPDATE_HEARING_REQUIREMENTS);
                    }
                }

                if (hasHomeOfficeRole && isAcceleratedDetainedAppeal(asylumCase)
                        && !List.of(PENDING_PAYMENT, AWAITING_REASONS_FOR_APPEAL,
                        AWAITING_CLARIFYING_QUESTIONS_ANSWERS, AWAITING_CMA_REQUIREMENTS,
                        REASONS_FOR_APPEAL_SUBMITTED).contains(currentState)) {

                    addType(values, ADJOURN, EXPEDITE);
                }

                addType(values, TIME_EXTENSION);
                addCommonEndStates(values);
                addType(values, CHANGE_DECISION_TYPE);
                break;

            case FTPA_SUBMITTED:
            case FTPA_DECIDED:
                addType(values, TIME_EXTENSION);
                addJudgeReview(values, hasPrivilegedRole(hasHomeOfficeRole, isInternalAndAdminRole, hasRole(ROLE_LEGAL_REP)));
                addIf(hasRole(ROLE_LEGAL_REP), values, UPDATE_APPEAL_DETAILS);
                addIf(isInternalAndAdminRole && isAcceleratedDetainedAppeal(asylumCase), values,
                        UPDATE_HEARING_REQUIREMENTS, UPDATE_APPEAL_DETAILS);
                addType(values, TIME_EXTENSION);
                addCommonEndStates(values);
                break;

            case FINAL_BUNDLING:
            case LISTING:
            case ADJOURNED:
            case PREPARE_FOR_HEARING:
            case PRE_HEARING:
            case DECISION:
                addJudgeReview(values, hasPrivilegedRole(hasHomeOfficeRole, isInternalAndAdminRole, hasRole(ROLE_LEGAL_REP)));
                addType(values, TIME_EXTENSION, ADJOURN, EXPEDITE);
                addIf(hasRole(ROLE_LEGAL_REP), values, UPDATE_APPEAL_DETAILS, UPDATE_HEARING_REQUIREMENTS);
                if (isAcceleratedDetainedAppeal(asylumCase)) {
                    addIf(hasRole(ROLE_LEGAL_REP), values, TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS);
                    addIf(isInternalAndAdminRole, values,
                            ADJOURN, EXPEDITE, TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS, UPDATE_APPEAL_DETAILS);
                    addIf(isInternalAndAdminRole && hasSubmittedHearingRequirements(asylumCase), values, UPDATE_HEARING_REQUIREMENTS);
                    addIf(hasHomeOfficeRole, values, ADJOURN, EXPEDITE);
                } else {
                    addType(values, TRANSFER);
                }
                addCommonEndStates(values);
                addType(values, CHANGE_DECISION_TYPE);
                break;

            case DECIDED:
                addJudgeReview(values, hasPrivilegedRole(hasHomeOfficeRole, isInternalAndAdminRole, hasRole(ROLE_LEGAL_REP)));
                addIf(hasRole(ROLE_LEGAL_REP), values, UPDATE_APPEAL_DETAILS);
                addIf(isAcceleratedDetainedAppeal(asylumCase) && hasRole(ROLE_LEGAL_REP), values, TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS);
                addIf(isInternalAndAdminRole && isAcceleratedDetainedAppeal(asylumCase), values,
                        TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS, UPDATE_APPEAL_DETAILS);
                addType(values, LINK_OR_UNLINK, SET_ASIDE_A_DECISION, APPLICATION_UNDER_RULE_31_OR_RULE_32, OTHER);
                break;

            case REMITTED:
                addType(values, EXPEDITE, LINK_OR_UNLINK, OTHER, TIME_EXTENSION, TRANSFER);
                addIf(hasRole(ROLE_LEGAL_REP), values, UPDATE_APPEAL_DETAILS);
                addType(values, WITHDRAW);
                addType(values, JUDGE_REVIEW);
                break;

            default:
                break;

        }

        if (shouldAddExpediteApplicationType(currentState, values)) {
            addType(values, EXPEDITE);
        }

        return values.isEmpty() ? new DynamicList("") : new DynamicList(values.get(0), values);
    }

    private void addJudgeReview(List<Value> values, boolean privileged) {
        addType(values, privileged ? JUDGE_REVIEW_LO : JUDGE_REVIEW);
    }

    private boolean hasPrivilegedRole(boolean hasHomeOfficeRole, boolean isInternalAndAdminRole, boolean hasLegalRepRole) {
        return hasHomeOfficeRole || isInternalAndAdminRole || hasLegalRepRole;
    }

    private void addCommonEndStates(List<Value> values) {
        addType(values, WITHDRAW, LINK_OR_UNLINK, OTHER);
    }

    private void addIf(boolean condition, List<Value> values, MakeAnApplicationTypes... types) {
        if (condition) {
            addType(values, types);
        }
    }

    private void addType(List<Value> values, MakeAnApplicationTypes... types) {
        for (MakeAnApplicationTypes type : types) {
            values.add(new Value(type.name(), type.toString()));
        }
    }

    private boolean hasRole(String roleName) {
        return userDetails.getRoles().contains(roleName);
    }

    private boolean isAcceleratedDetainedAppeal(AsylumCase asylumCase) {
        return asylumCase.read(AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)
            .orElse(YesOrNo.NO)
            .equals(YesOrNo.YES);
    }

    private boolean hasSubmittedHearingRequirements(AsylumCase asylumCase) {
        return asylumCase.read(ADA_HEARING_REQUIREMENTS_TO_REVIEW, YesOrNo.class)
            .orElse(YesOrNo.NO)
            .equals(YesOrNo.YES);
    }

    private boolean shouldAddExpediteApplicationType(State state, List<Value> values) {
        Value expeditedValue = new Value(EXPEDITE.name(), EXPEDITE.toString());
        List<State> notAllowedStates = List.of(APPEAL_STARTED, APPEAL_STARTED_BY_ADMIN);

        boolean hasAppropriateRole = hasRole(ROLE_HO_RESPONDENT) || hasRole(ROLE_LEGAL_REP);

        boolean alreadyAdded = !values.isEmpty() && values.contains(expeditedValue);

        return !notAllowedStates.contains(state) && hasAppropriateRole && !alreadyAdded;
    }
}
