package uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_PARTIES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType.REP;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;

@Slf4j
@Component
public class AsylumCaseSendDirectionEventValidForJourneyTypeChecker implements EventValidChecker<AsylumCase> {
    @Override
    public EventValid check(Callback<AsylumCase> callback) {
        Event event = callback.getEvent();
        if (event == Event.SEND_DIRECTION) {
            final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
            final JourneyType journeyType = asylumCase.<JourneyType>read(AsylumCaseFieldDefinition.JOURNEY_TYPE).orElse(REP);

            Parties directionTo = asylumCase.read(SEND_DIRECTION_PARTIES, Parties.class)
                    .orElseThrow(() -> new IllegalStateException("sendDirectionParties is not present"));

            if (journeyType == JourneyType.AIP && directionTo != Parties.RESPONDENT) {
                log.info("Cannot send a direction for an AIP case");
                return new EventValid("You cannot use this function to send a direction to an appellant in person.");
            } else if (journeyType == REP && directionTo == Parties.APPELLANT) {
                log.info("Cannot send an appellant a direction for a repped case");
                return new EventValid("This is a legally represented case. You cannot select appellant as the recipient.");
            }
        }

        return new EventValid();
    }
}
