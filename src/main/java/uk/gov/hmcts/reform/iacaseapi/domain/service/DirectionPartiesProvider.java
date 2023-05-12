package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties.*;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;



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

        DynamicList dynamicList;
        final List<Value> values = new ArrayList<>();

        if (HandlerUtils.isAipJourney(asylumCase)) {
            values.add(new Value(RESPONDENT.name(), RESPONDENT.toString()));
            values.add(new Value(APPELLANT.name(), APPELLANT.toString()));
            values.add(new Value(RESPONDENT_AND_APPELLANT.name(), RESPONDENT_AND_APPELLANT.toString()));
        } else {
            values.add(new Value(LEGAL_REPRESENTATIVE.name(), LEGAL_REPRESENTATIVE.toString()));
            values.add(new Value(RESPONDENT.name(), RESPONDENT.toString()));
            values.add(new Value(BOTH.name(), BOTH.toString()));
            values.add(new Value(APPELLANT.name(), APPELLANT.toString()));
        }


        if (!values.isEmpty()) {
            dynamicList = new DynamicList(values.get(0), values);
        } else {
            dynamicList = new DynamicList("");
        }

        return dynamicList;
    }
}
