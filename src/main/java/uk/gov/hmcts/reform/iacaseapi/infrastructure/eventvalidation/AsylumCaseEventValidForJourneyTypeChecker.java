package uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType.REP;

import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;

@Slf4j
@Component
public class AsylumCaseEventValidForJourneyTypeChecker implements EventValidChecker<AsylumCase> {
    private final List<Event> aipOnlyEvent = Arrays.asList(
            Event.REQUEST_REASONS_FOR_APPEAL,
            Event.EDIT_REASONS_FOR_APPEAL,
            Event.SUBMIT_REASONS_FOR_APPEAL,
            Event.REQUEST_CLARIFYING_ANSWERS,
            Event.SUBMIT_CLARIFYING_ANSWERS,
            Event.SEND_DIRECTION_WITH_QUESTIONS);
    private final List<Event> reppedOnlyEvent = Arrays.asList(
            Event.UPLOAD_RESPONDENT_EVIDENCE,
            Event.REQUEST_CASE_BUILDING
    );

    @Override
    public EventValid check(Callback<AsylumCase> callback) {
        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        final JourneyType journeyType = asylumCase.<JourneyType>read(AsylumCaseFieldDefinition.JOURNEY_TYPE).orElse(REP);
        final Event event = callback.getEvent();

        if (aipOnlyEvent.contains(event) && !AIP.equals(journeyType)) {
            log.info(String.format("[%s] is invalid for case id [%s] the hearing must be submitted by an appellant to handle this event.", event, callback.getCaseDetails().getId()));
            return new EventValid("You've made an invalid request. The hearing must be submitted by an appellant to make this request.");
        }
        if (reppedOnlyEvent.contains(event) && !REP.equals(journeyType)) {
            log.info(String.format("[%s] is invalid for case id [%s] the hearing must be submitted by a representative to handle this event.", event, callback.getCaseDetails().getId()));
            return new EventValid("You've made an invalid request. The hearing must be submitted by a representative to make this request.");
        }

        return new EventValid();
    }
}
