package uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_PARTIES;

import java.util.Arrays;
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

            if (HandlerUtils.isRepJourney(asylumCase) && Arrays.asList(Parties.APPELLANT_AND_RESPONDENT, Parties.APPELLANT).contains(directionTo)) {
                log.error("Cannot send an appellant a direction for a repped case");
                return new EventValid("This is a legally represented case. You cannot select appellant as the recipient.");
            }
            if (HandlerUtils.isAipJourney(asylumCase) && Arrays.asList(Parties.LEGAL_REPRESENTATIVE, Parties.BOTH).contains(directionTo)) {
                log.error("Cannot send a legal representative a direction for an appellant in person case");
                return new EventValid("This is an appellant in person case. You cannot select legal representative as the recipient.");
            }
        }

        return new EventValid();
    }
}

