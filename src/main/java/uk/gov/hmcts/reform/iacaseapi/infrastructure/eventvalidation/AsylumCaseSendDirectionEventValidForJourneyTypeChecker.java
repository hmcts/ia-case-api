package uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_PARTIES;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;

@Slf4j
@Component
public class AsylumCaseSendDirectionEventValidForJourneyTypeChecker implements EventValidChecker<AsylumCase> {
    @Override
    public EventValid check(Callback<AsylumCase> callback) {
        Event event = callback.getEvent();
        if (event == Event.SEND_DIRECTION) {
            final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

            Parties directionTo = asylumCase.read(SEND_DIRECTION_PARTIES, Parties.class)
                    .orElseThrow(() -> new IllegalStateException("sendDirectionParties is not present"));

            if (HandlerUtils.isAipJourney(asylumCase) && directionTo != Parties.RESPONDENT) {
                log.error("Cannot send a direction for an AIP case to appellant");
                return new EventValid("You cannot use this function to send a direction to an appellant in person.");
            } else if (HandlerUtils.isRepJourney(asylumCase) && List.of(Parties.APPELLANT_AND_RESPONDENT, Parties.APPELLANT).contains(directionTo)) {
                log.error("Cannot send an appellant a direction for a repped case");
                return new EventValid("This is a legally represented case. You cannot select appellant as the recipient.");
            } else if (HandlerUtils.isInternalCase(asylumCase) && (directionTo == Parties.BOTH || directionTo == Parties.LEGAL_REPRESENTATIVE)) {
                log.error("Cannot send legal representative a direction for an internal case");
                return new EventValid("This is an appellant in person case. You cannot select legal representative as the recipient.");
            }
        }

        return new EventValid();
    }
}

