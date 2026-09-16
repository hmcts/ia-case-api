package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfTimeDecisionDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
public class OutOfTimeDecisionDetailsAppender {

    public List<IdValue<OutOfTimeDecisionDetails>> append(
        List<IdValue<OutOfTimeDecisionDetails>> existingOutOfTimeDecisionDetails,
        OutOfTimeDecisionDetails outOfTimeDecisionDetails
    ) {

        List<IdValue<OutOfTimeDecisionDetails>> allOutOfTimeDecisionDetails = new ArrayList<>();

        int index = existingOutOfTimeDecisionDetails.size() + 1;

        allOutOfTimeDecisionDetails.add(new IdValue<>(String.valueOf(index--), outOfTimeDecisionDetails));

        for (IdValue<OutOfTimeDecisionDetails> existingOutOfTimeDecision : existingOutOfTimeDecisionDetails) {
            allOutOfTimeDecisionDetails.add(new IdValue<>(String.valueOf(index--), existingOutOfTimeDecision.getValue()));
        }

        return allOutOfTimeDecisionDetails;
    }
}
