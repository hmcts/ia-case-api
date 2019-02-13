package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@Service
public class DirectionPartiesResolver {

    public Parties resolve(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        switch (callback.getEvent()) {

            case REQUEST_CASE_EDIT:
                return Parties.LEGAL_REPRESENTATIVE;

            case REQUEST_RESPONDENT_EVIDENCE:
            case REQUEST_RESPONDENT_REVIEW:
                return Parties.RESPONDENT;

            case SEND_DIRECTION:
                return
                    callback
                        .getCaseDetails()
                        .getCaseData()
                        .getSendDirectionParties()
                        .orElseThrow(() -> new IllegalStateException("sendDirectionParties is not present"));

            default:
                throw new IllegalArgumentException("Callback event is not for sending a direction");
        }
    }
}
