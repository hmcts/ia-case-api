package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties.*;

@Service
public class DirectionPartiesProvider {


    private final UserDetails userDetails;

    public DirectionPartiesProvider(
        UserDetails userDetails
    ) {
        this.userDetails = userDetails;
    }

    public DynamicList getDirectionParties(Callback<AsylumCase> callback) {

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final State currentState = callback.getCaseDetails().getState();

        DynamicList dynamicList;
        final List<Value> values = new ArrayList<>();
        switch (currentState) {
            case APPEAL_SUBMITTED:
            case ENDED:
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
            case FTPA_SUBMITTED:
            case FTPA_DECIDED:
            case FINAL_BUNDLING:
            case LISTING:
            case ADJOURNED:
            case PREPARE_FOR_HEARING:
            case PRE_HEARING:
            case DECISION:
            case DECIDED:
            default:
                
                if(HandlerUtils.isAipJourney(asylumCase)) {
                    values.add(new Value(RESPONDENT.name(), RESPONDENT.toString()));
                    values.add(new Value(APPELLANT.name(), APPELLANT.toString()));
                    values.add(new Value(RESPONDENT_AND_APPELLANT.name(), RESPONDENT_AND_APPELLANT.toString()));
                } else {
                    values.add(new Value(LEGAL_REPRESENTATIVE.name(), LEGAL_REPRESENTATIVE.toString()));
                    values.add(new Value(RESPONDENT.name(), RESPONDENT.toString()));
                    values.add(new Value(BOTH.name(), BOTH.toString()));
                    values.add(new Value(APPELLANT.name(), APPELLANT.toString()));
                }

                break;

        }

        if (!values.isEmpty()) {
            dynamicList = new DynamicList(values.get(0), values);
        } else {
            dynamicList = new DynamicList("");
        }

        return dynamicList;
    }
}
