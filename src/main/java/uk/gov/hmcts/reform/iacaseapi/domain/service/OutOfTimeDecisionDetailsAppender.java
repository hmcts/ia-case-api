package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfTimeDecisionDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class OutOfTimeDecisionDetailsAppender {

    private List<IdValue<OutOfTimeDecisionDetails>> allOutOfTimeDecisionDetails;

    public List<IdValue<OutOfTimeDecisionDetails>> getAllOutOfTimeDecisionDetails() {
        return allOutOfTimeDecisionDetails;
    }

    public void clearAllOutOfTimeDecisionDetails() {
        this.allOutOfTimeDecisionDetails = null;
    }

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
