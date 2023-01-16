package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.*;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@Service
public class MakeAnApplicationTypesProvider {

    private static final String ROLE_LEGAL_REP = "caseworker-ia-legalrep-solicitor";
    private static final String ROLE_HO_APC = "caseworker-ia-homeofficeapc";
    private static final String ROLE_HO_LART = "caseworker-ia-homeofficelart";
    private static final String ROLE_HO_POU = "caseworker-ia-homeofficepou";
    private static final String ROLE_HO_RESPONDENT = "caseworker-ia-respondentofficer";
    private static final List<String> HO_ROLES = List.of(ROLE_HO_APC, ROLE_HO_LART, ROLE_HO_POU, ROLE_HO_RESPONDENT);

    private final UserDetails userDetails;

    public MakeAnApplicationTypesProvider(
        UserDetails userDetails
    ) {
        this.userDetails = userDetails;
    }

    public DynamicList getMakeAnApplicationTypes(Callback<AsylumCase> callback) {

        final State currentState = callback.getCaseDetails().getState();

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        boolean hasHomeOfficeRole = userDetails.getRoles()
            .stream().anyMatch(HO_ROLES::contains);

        DynamicList dynamicList;
        final List<Value> values = new ArrayList<>();
        switch (currentState) {
            case APPEAL_SUBMITTED:
                values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));

                if (hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                }

                if (hasRole(ROLE_LEGAL_REP) || hasHomeOfficeRole) {
                    values.remove(0); // remove JUDGE_REVIEW
                    values.add(new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()));
                }

                if (isAcceleratedDetainedAppeal(asylumCase) && hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                        TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()));
                }

                values.add(new Value(WITHDRAW.name(), WITHDRAW.toString()));
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));

                values.add(new Value(OTHER.name(), OTHER.toString()));
                break;

            case ENDED:
                if (hasRole(ROLE_LEGAL_REP) || hasHomeOfficeRole) {
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
                if (hasRole(ROLE_LEGAL_REP) || hasHomeOfficeRole) {
                    values.add(new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()));
                } else {
                    values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));
                }

                if (hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));

                    if (isAcceleratedDetainedAppeal(asylumCase) && currentState != PENDING_PAYMENT) {
                        values.add(new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                            TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()));
                    }
                }

                values.add(new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()));
                values.add(new Value(WITHDRAW.name(), WITHDRAW.toString()));
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                values.add(new Value(OTHER.name(), OTHER.toString()));
                break;

            case FTPA_SUBMITTED:
            case FTPA_DECIDED:
                if (hasRole(ROLE_LEGAL_REP) || hasHomeOfficeRole) {
                    values.add(new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()));
                } else {
                    values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));
                }

                if (hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                }

                values.add(new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()));
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                values.add(new Value(OTHER.name(), OTHER.toString()));
                break;

            case FINAL_BUNDLING:
                if (hasRole(ROLE_LEGAL_REP) || hasHomeOfficeRole) {
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
                        values.add(new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                            TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()));
                    }
                }
                values.add(new Value(WITHDRAW.name(), WITHDRAW.toString()));
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                values.add(new Value(OTHER.name(), OTHER.toString()));
                break;

            case LISTING:
                if (hasRole(ROLE_LEGAL_REP) || hasHomeOfficeRole) {
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
                        values.add(new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                            TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()));
                    }
                }
                values.add(new Value(WITHDRAW.name(), WITHDRAW.toString()));
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                break;

            case ADJOURNED:
            case PREPARE_FOR_HEARING:
            case PRE_HEARING:
            case DECISION:
                if (hasRole(ROLE_LEGAL_REP) || hasHomeOfficeRole) {
                    values.add(new Value(JUDGE_REVIEW_LO.name(), JUDGE_REVIEW_LO.toString()));
                } else {
                    values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));
                }

                values.add(new Value(ADJOURN.name(), ADJOURN.toString()));
                values.add(new Value(EXPEDITE.name(), EXPEDITE.toString()));
                values.add(new Value(TRANSFER.name(), TRANSFER.toString()));
                values.add(new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()));

                if (hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                    values.add(new Value(UPDATE_HEARING_REQUIREMENTS.name(),
                        UPDATE_HEARING_REQUIREMENTS.toString()));

                    if (isAcceleratedDetainedAppeal(asylumCase)) {
                        values.add(new Value(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.name(),
                            TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString()));
                    }
                }
                values.add(new Value(WITHDRAW.name(), WITHDRAW.toString()));
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                values.add(new Value(OTHER.name(), OTHER.toString()));
                break;

            case DECIDED:
                if (hasRole(ROLE_LEGAL_REP) || hasHomeOfficeRole) {
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
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                values.add(new Value(OTHER.name(), OTHER.toString()));
                break;

            default:
                break;

        }

        if (!values.isEmpty()) {
            dynamicList = new DynamicList(values.get(0), values);
        } else {
            dynamicList = new DynamicList("");
        }

        return dynamicList;
    }

    private boolean hasRole(String roleName) {

        return userDetails
            .getRoles()
            .contains(roleName);
    }

    private boolean isAcceleratedDetainedAppeal(AsylumCase asylumCase) {
        return asylumCase.read(AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)
            .orElse(YesOrNo.NO)
            .equals(YesOrNo.YES);
    }
}
