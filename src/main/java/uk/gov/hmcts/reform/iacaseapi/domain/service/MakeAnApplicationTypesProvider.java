package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.*;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@Service
public class MakeAnApplicationTypesProvider {

    private static final String ROLE_LEGAL_REP = "caseworker-ia-legalrep-solicitor";

    private final UserDetails userDetails;

    public MakeAnApplicationTypesProvider(
        UserDetails userDetails
    ) {
        this.userDetails = userDetails;
    }

    public DynamicList getMakeAnApplicationTypes(Callback<AsylumCase> callback) {

        final State currentState = callback.getCaseDetails().getState();

        DynamicList dynamicList;
        final List<Value> values = new ArrayList<>();
        switch (currentState) {
            case APPEAL_SUBMITTED:
                if (hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                }
                values.add(new Value(WITHDRAW.name(), WITHDRAW.toString()));
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));
                values.add(new Value(OTHER.name(), OTHER.toString()));
                values.add(new Value(CHANGE_HEARING_TYPE.name(), CHANGE_HEARING_TYPE.toString()));
                break;

            case ENDED:
                values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));
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
                if (hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                }
                values.add(new Value(WITHDRAW.name(), WITHDRAW.toString()));
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));
                values.add(new Value(OTHER.name(), OTHER.toString()));
                values.add(new Value(CHANGE_HEARING_TYPE.name(), CHANGE_HEARING_TYPE.toString()));
                break;

            case FTPA_SUBMITTED:
            case FTPA_DECIDED:
                values.add(new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()));
                if (hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                }
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));
                values.add(new Value(OTHER.name(), OTHER.toString()));
                break;

            case FINAL_BUNDLING:
                values.add(new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()));
                if (hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                    values.add(new Value(UPDATE_HEARING_REQUIREMENTS.name(),
                        UPDATE_HEARING_REQUIREMENTS.toString()));
                }
                values.add(new Value(WITHDRAW.name(), WITHDRAW.toString()));
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));
                values.add(new Value(OTHER.name(), OTHER.toString()));
                break;

            case LISTING:
                values.add(new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()));
                if (hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                    values.add(new Value(UPDATE_HEARING_REQUIREMENTS.name(),
                        UPDATE_HEARING_REQUIREMENTS.toString()));
                }
                values.add(new Value(WITHDRAW.name(), WITHDRAW.toString()));
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));
                values.add(new Value(CHANGE_HEARING_TYPE.name(), CHANGE_HEARING_TYPE.toString()));
                break;

            case ADJOURNED:
            case PREPARE_FOR_HEARING:
            case PRE_HEARING:
            case DECISION:
                values.add(new Value(ADJOURN.name(), ADJOURN.toString()));
                values.add(new Value(EXPEDITE.name(), EXPEDITE.toString()));
                values.add(new Value(TRANSFER.name(), TRANSFER.toString()));
                values.add(new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()));
                if (hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                    values.add(new Value(UPDATE_HEARING_REQUIREMENTS.name(),
                        UPDATE_HEARING_REQUIREMENTS.toString()));
                }
                values.add(new Value(WITHDRAW.name(), WITHDRAW.toString()));
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));
                values.add(new Value(OTHER.name(), OTHER.toString()));
                values.add(new Value(CHANGE_HEARING_TYPE.name(), CHANGE_HEARING_TYPE.toString()));
                break;

            case DECIDED:
                if (hasRole(ROLE_LEGAL_REP)) {
                    values.add(new Value(UPDATE_APPEAL_DETAILS.name(),
                        UPDATE_APPEAL_DETAILS.toString()));
                }
                values.add(new Value(LINK_OR_UNLINK.name(), LINK_OR_UNLINK.toString()));
                values.add(new Value(JUDGE_REVIEW.name(), JUDGE_REVIEW.toString()));
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
}
