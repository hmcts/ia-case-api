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

        DynamicList dynamicList;
        final List<Value> values = new ArrayList<>();
        switch (currentState) {

            case APPEAL_SUBMITTED:
                addValues(values, JUDGE_REVIEW);
                if (hasRole(ROLE_LEGAL_REP)) {
                    addValues(values, UPDATE_APPEAL_DETAILS);
                }

                if (hasRole(ROLE_LEGAL_REP) || hasHomeOfficeRole || isInternalAndAdminRole) {
                    values.remove(0); // remove JUDGE_REVIEW
                    addValues(values, JUDGE_REVIEW_LO);
                }

                if (isAcceleratedDetainedAppeal(asylumCase) && hasRole(ROLE_LEGAL_REP)) {
                    addValues(values, TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS);
                }

                if (isAcceleratedDetainedAppeal(asylumCase) && isInternalAndAdminRole) {
                    addValues(values, TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS, UPDATE_APPEAL_DETAILS);
                }

                addValues(values, WITHDRAW, LINK_OR_UNLINK, OTHER, CHANGE_DECISION_TYPE);
                break;

            case ENDED:
                addJudgeReviewValues(values, hasHomeOfficeRole, isInternalAndAdminRole);
                addValues(values, REINSTATE);
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
                addValues(values, TIME_EXTENSION);

                if (isInternalAndAdminRole && isAcceleratedDetainedAppeal(asylumCase)) {
                    addValues(values, ADJOURN, EXPEDITE);
                    if (hasSubmittedHearingRequirements(asylumCase)) {
                        addValues(values, UPDATE_HEARING_REQUIREMENTS);
                    }
                    if (currentState != PENDING_PAYMENT) {
                        addValues(values, TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS, UPDATE_APPEAL_DETAILS);
                    }
                }

                addJudgeReviewValues(values, hasHomeOfficeRole, isInternalAndAdminRole);

                if (hasRole(ROLE_LEGAL_REP)) {
                    addValues(values, UPDATE_APPEAL_DETAILS);
                    if (isAcceleratedDetainedAppeal(asylumCase) && currentState != PENDING_PAYMENT) {
                        addValues(values, ADJOURN, EXPEDITE,
                                TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS, UPDATE_HEARING_REQUIREMENTS);
                    }
                }

                if (hasHomeOfficeRole && isAcceleratedDetainedAppeal(asylumCase)
                        && currentState != PENDING_PAYMENT
                        && currentState != AWAITING_REASONS_FOR_APPEAL
                        && currentState != AWAITING_CLARIFYING_QUESTIONS_ANSWERS
                        && currentState != AWAITING_CMA_REQUIREMENTS
                        && currentState != REASONS_FOR_APPEAL_SUBMITTED) {
                    addValues(values, ADJOURN, EXPEDITE);
                }

                addValues(values, TIME_EXTENSION, WITHDRAW, LINK_OR_UNLINK, OTHER, CHANGE_DECISION_TYPE);
                break;

            case FTPA_SUBMITTED:
            case FTPA_DECIDED:
                addValues(values, TIME_EXTENSION);
                addJudgeReviewValues(values, hasHomeOfficeRole, isInternalAndAdminRole);

                if (hasRole(ROLE_LEGAL_REP)) {
                    addValues(values, UPDATE_APPEAL_DETAILS);
                }

                if (isInternalAndAdminRole && isAcceleratedDetainedAppeal(asylumCase)) {
                    if (hasSubmittedHearingRequirements(asylumCase)) {
                        addValues(values, UPDATE_HEARING_REQUIREMENTS);
                    }
                    addValues(values, UPDATE_APPEAL_DETAILS);
                }

                addValues(values, TIME_EXTENSION, LINK_OR_UNLINK, OTHER);
                break;

            case FINAL_BUNDLING:
                addJudgeReviewValues(values, hasHomeOfficeRole, isInternalAndAdminRole);
                addValues(values, TIME_EXTENSION);

                if (hasRole(ROLE_LEGAL_REP)) {
                    addValues(values, UPDATE_APPEAL_DETAILS, UPDATE_HEARING_REQUIREMENTS);
                    if (isAcceleratedDetainedAppeal(asylumCase)) {
                        addValues(values, ADJOURN, EXPEDITE, TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS);
                    }
                }

                if (hasHomeOfficeRole && isAcceleratedDetainedAppeal(asylumCase)) {
                    addValues(values, ADJOURN, EXPEDITE);
                }

                if (!isAcceleratedDetainedAppeal(asylumCase)) {
                    addValues(values, TRANSFER);
                }

                if (isInternalAndAdminRole
                        && isAcceleratedDetainedAppeal(asylumCase)) {
                    addValues(values, ADJOURN, EXPEDITE, TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS, UPDATE_APPEAL_DETAILS);
                    if (hasSubmittedHearingRequirements(asylumCase)) {
                        addValues(values, UPDATE_HEARING_REQUIREMENTS);
                    }
                }

                addValues(values, WITHDRAW, LINK_OR_UNLINK, CHANGE_DECISION_TYPE, OTHER);
                break;

            case LISTING:
                if (hasRole(ROLE_LEGAL_REP) || hasHomeOfficeRole || isInternalAndAdminRole) {
                    addValues(values, JUDGE_REVIEW_LO);
                } else {
                    addValues(values, JUDGE_REVIEW);
                }

                addValues(values, TIME_EXTENSION);

                if (hasRole(ROLE_LEGAL_REP)) {
                    addValues(values, UPDATE_APPEAL_DETAILS, UPDATE_HEARING_REQUIREMENTS);

                    if (isAcceleratedDetainedAppeal(asylumCase)) {
                        addValues(values, ADJOURN, EXPEDITE, TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS);
                    }
                }

                if (hasHomeOfficeRole && isAcceleratedDetainedAppeal(asylumCase)) {
                    addValues(values, ADJOURN, EXPEDITE);
                }

                if (isInternalAndAdminRole
                    && isAcceleratedDetainedAppeal(asylumCase)) {
                    addValues(values, ADJOURN, EXPEDITE, TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS, UPDATE_APPEAL_DETAILS);
                    if (hasSubmittedHearingRequirements(asylumCase)) {
                        addValues(values, UPDATE_HEARING_REQUIREMENTS);
                    }
                }

                addValues(values, WITHDRAW, LINK_OR_UNLINK, CHANGE_DECISION_TYPE);
                break;

            case ADJOURNED:
            case PREPARE_FOR_HEARING:
            case PRE_HEARING:
            case DECISION:
                addJudgeReviewValues(values, hasHomeOfficeRole, isInternalAndAdminRole);
                addValues(values, ADJOURN, EXPEDITE);
                addTransferOutValues(values, asylumCase);
                addValues(values, TIME_EXTENSION);

                if (hasRole(ROLE_LEGAL_REP)) {
                    addValues(values, UPDATE_APPEAL_DETAILS, UPDATE_HEARING_REQUIREMENTS);
                }

                if (isInternalAndAdminRole && isAcceleratedDetainedAppeal(asylumCase)) {
                    if (hasSubmittedHearingRequirements(asylumCase)) {
                        addValues(values, UPDATE_HEARING_REQUIREMENTS);
                    }
                    addValues(values, UPDATE_APPEAL_DETAILS, TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS);
                }

                addValues(values, WITHDRAW, LINK_OR_UNLINK, OTHER, CHANGE_DECISION_TYPE);
                break;

            case DECIDED:
                addJudgeReviewValues(values, hasHomeOfficeRole, isInternalAndAdminRole);
                if (hasRole(ROLE_LEGAL_REP)) {
                    addValues(values, UPDATE_APPEAL_DETAILS);
                    if (isAcceleratedDetainedAppeal(asylumCase)) {
                        addValues(values, TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS);
                    }
                }
                if (isInternalAndAdminRole && isAcceleratedDetainedAppeal(asylumCase)) {
                    addValues(values, TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS, UPDATE_APPEAL_DETAILS);
                }

                addValues(values, LINK_OR_UNLINK, SET_ASIDE_A_DECISION,
                        APPLICATION_UNDER_RULE_31_OR_RULE_32, OTHER);
                break;

            case REMITTED:
                addValues(values, EXPEDITE, LINK_OR_UNLINK, OTHER, TIME_EXTENSION, TRANSFER);
                if (hasRole(ROLE_LEGAL_REP)) {
                    addValues(values, UPDATE_APPEAL_DETAILS);
                }
                addValues(values, WITHDRAW, JUDGE_REVIEW);
                break;

            default:
                break;
        }

        if (shouldAddExpediteApplicationType(currentState, values)) {
            addValues(values, EXPEDITE);
        }

        if (!values.isEmpty()) {
            dynamicList = new DynamicList(values.get(0), values);
        } else {
            dynamicList = new DynamicList("");
        }

        return dynamicList;
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

    private void addValue(List<Value> values, MakeAnApplicationTypes type) {
        values.add(new Value(type.name(), type.toString()));
    }

    private void addValues(List<Value> values, MakeAnApplicationTypes... types) {
        for (MakeAnApplicationTypes type : types) {
            addValue(values, type);
        }
    }

    private void addJudgeReviewValues(List<Value> values, boolean hasHomeOfficeRole, boolean isInternalAndAdminRole) {
        if (hasRole(ROLE_LEGAL_REP) || hasHomeOfficeRole || isInternalAndAdminRole) {
            addValues(values, JUDGE_REVIEW_LO);
        } else {
            addValues(values, JUDGE_REVIEW);
        }
    }
}
