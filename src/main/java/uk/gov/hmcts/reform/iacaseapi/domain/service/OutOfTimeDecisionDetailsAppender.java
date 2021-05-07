package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfTimeDecisionDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
@Setter
@Getter
public class OutOfTimeDecisionDetailsAppender {

    private List<IdValue<OutOfTimeDecisionDetails>> allOutOfTimeDecisionDetails;

    public List<IdValue<OutOfTimeDecisionDetails>> append(
        List<IdValue<OutOfTimeDecisionDetails>> existingOutOfTimeDecisionDetails,
        OutOfTimeDecisionDetails outOfTimeDecisionDetails
    ) {

        allOutOfTimeDecisionDetails = new ArrayList<>();

        int index = existingOutOfTimeDecisionDetails.size() + 1;

        allOutOfTimeDecisionDetails.add(new IdValue<>(String.valueOf(index--), outOfTimeDecisionDetails));

        for (IdValue<OutOfTimeDecisionDetails> existingOutOfTimeDecision : existingOutOfTimeDecisionDetails) {
            allOutOfTimeDecisionDetails.add(new IdValue<>(String.valueOf(index--), existingOutOfTimeDecision.getValue()));
        }

        return allOutOfTimeDecisionDetails;
    }
}
