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
                values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));

                if (hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                }

                if (hasRole(ROLE_LEGAL_REP) || hasHomeOfficeRole || isInternalAndAdminRole) {
                    values.remove(0); // remove JUDGE_REVIEW
                    values.add(new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()));
                }

                if (isAcceleratedDetainedAppeal(asylumCase) && hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                        TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()));
                }

                if (isAcceleratedDetainedAppeal(asylumCase) && isInternalAndAdminRole) {
                    values.add(new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                        TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()));
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                }

                values.add(new Value(WITHDRAW.name(), WITHDRAW.toString()));
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                values.add(new Value(OTHER.name(), OTHER.toString()));
                addValue(values, CHANGE_HEARING_TYPE);
                break;

            case ENDED:
                if (hasRole(ROLE_LEGAL_REP) || hasHomeOfficeRole || isInternalAndAdminRole) {
                    values.add(new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()));
                } else {
                    values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));
                }

                values.add(new Value(REINSTATE.name(), REINSTATE.toString()));
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
                values.add(new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()));
                if (isInternalAndAdminRole
                    && isAcceleratedDetainedAppeal(asylumCase)) {

                    values.add(new Value(ADJOURN.name(), ADJOURN.toString()));
                    values.add(new Value(EXPEDITE.name(), EXPEDITE.toString()));

                    if (hasSubmittedHearingRequirements(asylumCase)) {
                        values.add(new Value(UPDATE_HEARING_REQUIREMENTS.name(),
                            UPDATE_HEARING_REQUIREMENTS.toString()));
                    }
                    if (currentState != PENDING_PAYMENT) {
                        values.add(new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                            TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()));
                        values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                            UPDATE_APPEAL_DETAILS.toString()));
                    }
                }

                if (hasRole(ROLE_LEGAL_REP) || hasHomeOfficeRole || isInternalAndAdminRole) {
                    values.add(new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()));
                } else {
                    values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));
                }

                if (hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));

                    if (isAcceleratedDetainedAppeal(asylumCase) && currentState != PENDING_PAYMENT) {
                        values.add(new Value(ADJOURN.name(), ADJOURN.toString()));
                        values.add(new Value(EXPEDITE.name(), EXPEDITE.toString()));
                        values.add(new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                            TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()));
                        values.add(new Value(UPDATE_HEARING_REQUIREMENTS.name(), UPDATE_HEARING_REQUIREMENTS.toString()));
                    }
                }

                if (hasHomeOfficeRole && isAcceleratedDetainedAppeal(asylumCase)
                    && currentState != PENDING_PAYMENT
                    && currentState != AWAITING_REASONS_FOR_APPEAL
                    && currentState != AWAITING_CLARIFYING_QUESTIONS_ANSWERS
                    && currentState != AWAITING_CMA_REQUIREMENTS
                    && currentState != REASONS_FOR_APPEAL_SUBMITTED) {

                    values.add(new Value(ADJOURN.name(), ADJOURN.toString()));
                    values.add(new Value(EXPEDITE.name(), EXPEDITE.toString()));
                }

                values.add(new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()));
                values.add(new Value(WITHDRAW.name(), WITHDRAW.toString()));
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                values.add(new Value(OTHER.name(), OTHER.toString()));
                addValue(values, CHANGE_HEARING_TYPE);
                break;

            case FTPA_SUBMITTED:
            case FTPA_DECIDED:
                values.add(new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()));
                if (hasRole(ROLE_LEGAL_REP) || hasHomeOfficeRole || isInternalAndAdminRole) {
                    values.add(new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()));
                } else {
                    values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));
                }

                if (hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                }

                if (isInternalAndAdminRole
                    && isAcceleratedDetainedAppeal(asylumCase)) {
                    if (hasSubmittedHearingRequirements(asylumCase)) {
                        values.add(new Value(UPDATE_HEARING_REQUIREMENTS.name(),
                            UPDATE_HEARING_REQUIREMENTS.toString()));
                    }
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                }

                values.add(new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()));
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                values.add(new Value(OTHER.name(), OTHER.toString()));
                break;

            case FINAL_BUNDLING:
                if (hasRole(ROLE_LEGAL_REP) || hasHomeOfficeRole || isInternalAndAdminRole) {
                    values.add(new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()));
                } else {
                    values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));
                }

                values.add(new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()));

                if (hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                    values.add(new Value(UPDATE_HEARING_REQUIREMENTS.name(),
                        UPDATE_HEARING_REQUIREMENTS.toString()));

                    if (isAcceleratedDetainedAppeal(asylumCase)) {
                        values.add(new Value(ADJOURN.name(), ADJOURN.toString()));
                        values.add(new Value(EXPEDITE.name(), EXPEDITE.toString()));
                        values.add(new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                            TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()));
                    }
                }

                if (hasHomeOfficeRole && isAcceleratedDetainedAppeal(asylumCase)) {
                    values.add(new Value(ADJOURN.name(), ADJOURN.toString()));
                    values.add(new Value(EXPEDITE.name(), EXPEDITE.toString()));
                }

                if (!isAcceleratedDetainedAppeal(asylumCase)) {
                    values.add(new Value(TRANSFER.name(), TRANSFER.toString()));
                }

                if (isInternalAndAdminRole
                    && isAcceleratedDetainedAppeal(asylumCase)) {
                    values.add(new Value(ADJOURN.name(), ADJOURN.toString()));
                    values.add(new Value(EXPEDITE.name(), EXPEDITE.toString()));
                    values.add(new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                        TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()));
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                    if (hasSubmittedHearingRequirements(asylumCase)) {
                        values.add(new Value(UPDATE_HEARING_REQUIREMENTS.name(),
                            UPDATE_HEARING_REQUIREMENTS.toString()));
                    }
                }

                values.add(new Value(WITHDRAW.name(), WITHDRAW.toString()));
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                addValue(values, CHANGE_HEARING_TYPE);
                values.add(new Value(OTHER.name(), OTHER.toString()));
                break;

            case LISTING:
                if (hasRole(ROLE_LEGAL_REP) || hasHomeOfficeRole || isInternalAndAdminRole) {
                    values.add(new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()));
                } else {
                    values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));
                }

                values.add(new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()));

                if (hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                    values.add(new Value(UPDATE_HEARING_REQUIREMENTS.name(),
                        UPDATE_HEARING_REQUIREMENTS.toString()));

                    if (isAcceleratedDetainedAppeal(asylumCase)) {
                        values.add(new Value(ADJOURN.name(), ADJOURN.toString()));
                        values.add(new Value(EXPEDITE.name(), EXPEDITE.toString()));
                        values.add(new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                            TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()));
                    }
                }

                if (hasHomeOfficeRole && isAcceleratedDetainedAppeal(asylumCase)) {
                    values.add(new Value(ADJOURN.name(), ADJOURN.toString()));
                    values.add(new Value(EXPEDITE.name(), EXPEDITE.toString()));
                }

                if (isInternalAndAdminRole
                    && isAcceleratedDetainedAppeal(asylumCase)) {
                    values.add(new Value(ADJOURN.name(), ADJOURN.toString()));
                    values.add(new Value(EXPEDITE.name(), EXPEDITE.toString()));
                    values.add(new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                        TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()));
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                    if (hasSubmittedHearingRequirements(asylumCase)) {
                        values.add(new Value(UPDATE_HEARING_REQUIREMENTS.name(),
                            UPDATE_HEARING_REQUIREMENTS.toString()));
                    }
                }

                values.add(new Value(WITHDRAW.name(), WITHDRAW.toString()));
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                addValue(values, CHANGE_HEARING_TYPE);
                break;

            case ADJOURNED:
            case PREPARE_FOR_HEARING:
            case PRE_HEARING:
            case DECISION:
                if (hasRole(ROLE_LEGAL_REP) || hasHomeOfficeRole || isInternalAndAdminRole) {
                    values.add(new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()));
                } else {
                    values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));
                }

                values.add(new Value(ADJOURN.name(), ADJOURN.toString()));
                values.add(new Value(EXPEDITE.name(), EXPEDITE.toString()));

                if (isAcceleratedDetainedAppeal(asylumCase) && hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                        TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()));
                } else if (!isAcceleratedDetainedAppeal(asylumCase)) {
                    values.add(new Value(TRANSFER.name(), TRANSFER.toString()));
                }

                values.add(new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()));

                if (hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                    values.add(new Value(UPDATE_HEARING_REQUIREMENTS.name(),
                        UPDATE_HEARING_REQUIREMENTS.toString()));
                }

                if (isInternalAndAdminRole
                    && isAcceleratedDetainedAppeal(asylumCase)) {
                    if (hasSubmittedHearingRequirements(asylumCase)) {
                        values.add(new Value(UPDATE_HEARING_REQUIREMENTS.name(),
                            UPDATE_HEARING_REQUIREMENTS.toString()));
                    }
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                    values.add(new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                        TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()));
                }

                values.add(new Value(WITHDRAW.name(), WITHDRAW.toString()));
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                values.add(new Value(OTHER.name(), OTHER.toString()));
                addValue(values, CHANGE_HEARING_TYPE);
                break;

            case DECIDED:
                if (hasRole(ROLE_LEGAL_REP) || hasHomeOfficeRole || isInternalAndAdminRole) {
                    values.add(new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()));
                } else {
                    values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));
                }

                if (hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));

                    if (isAcceleratedDetainedAppeal(asylumCase)) {
                        values.add(new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                            TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()));
                    }
                }
                if (isInternalAndAdminRole
                    && isAcceleratedDetainedAppeal(asylumCase)) {
                    values.add(new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                        TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()));
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                }

                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                values.add(new Value(SET_ASIDE_A_DECISION.name(), SET_ASIDE_A_DECISION.toString()));
                values.add(new Value(APPLICATION_UNDER_RULE_31_OR_RULE_32.name(), APPLICATION_UNDER_RULE_31_OR_RULE_32.toString()));
                values.add(new Value(OTHER.name(), OTHER.toString()));
                break;

            case REMITTED:
                values.add(new Value(EXPEDITE.name(), EXPEDITE.toString()));
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                values.add(new Value(OTHER.name(), OTHER.toString()));
                values.add(new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()));
                values.add(new Value(TRANSFER.name(), TRANSFER.toString()));
                if (hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()));
                }
                values.add(new Value(WITHDRAW.name(), WITHDRAW.toString()));
                values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));
                break;

            default:
                break;

        }

        if (shouldAddExpediteApplicationType(currentState, values)) {
            values.add(new Value(EXPEDITE.name(), EXPEDITE.toString()));
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

}
