package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_PARTIES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAppellantInDetention;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isInternalCase;

import java.util.Optional;
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

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        switch (callback.getEvent()) {

            case LIST_CASE:
            case REQUEST_CASE_EDIT:
            case REQUEST_CASE_BUILDING:
            case FORCE_REQUEST_CASE_BUILDING:
            case REQUEST_RESPONSE_REVIEW:
            case REQUEST_NEW_HEARING_REQUIREMENTS:
                return isInternalCase(asylumCase) && isAppellantInDetention(asylumCase)
                        ? Parties.APPELLANT
                        : Parties.LEGAL_REPRESENTATIVE;

            case REQUEST_RESPONSE_AMEND:
            case REQUEST_RESPONDENT_EVIDENCE:
            case REQUEST_RESPONDENT_REVIEW:
                return Parties.RESPONDENT;

            case SEND_DIRECTION:
                Optional<Parties> sendDirectionParties = callback
                        .getCaseDetails()
                        .getCaseData()
                        .read(SEND_DIRECTION_PARTIES);
                return
                    sendDirectionParties
                        .orElseThrow(() -> new IllegalStateException("sendDirectionParties is not present"));
            case REQUEST_REASONS_FOR_APPEAL:
                return Parties.APPELLANT;
            default:
                throw new IllegalArgumentException("Callback event is not for sending a direction");
        }
    }
}
