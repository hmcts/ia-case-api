package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.CMR_LISTING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.CMR_RE_LISTING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_CASE_LISTING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.LIST_CASE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_NEXT_HEARING_INFO;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.NextHearingDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NextHearingDateService;

@Slf4j
@Component
@RequiredArgsConstructor
public class NextHearingDateHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final NextHearingDateService nextHearingDateService;

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        List<Event> targetEvents = List.of(
            LIST_CASE,
            EDIT_CASE_LISTING,
            CMR_LISTING,
            CMR_RE_LISTING);

        return (callbackStage ==  PreSubmitCallbackStage.ABOUT_TO_START && callback.getEvent() == UPDATE_NEXT_HEARING_INFO)
                || (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && targetEvents.contains(callback.getEvent()));
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (nextHearingDateService.enabled()) {
            log.debug("Next hearing date feature enabled");
            NextHearingDetails nextHearingDetails;
            if (HandlerUtils.isIntegrated(asylumCase)) {
                nextHearingDetails = nextHearingDateService.calculateNextHearingDateFromHearings(callback);
            } else if (callback.getEvent() == UPDATE_NEXT_HEARING_INFO) {
                nextHearingDetails = NextHearingDetails.builder().hearingId(null).hearingDateTime(null).build();
            } else {
                nextHearingDetails = nextHearingDateService.calculateNextHearingDateFromCaseData(callback);
            }
            asylumCase.write(NEXT_HEARING_DETAILS, nextHearingDetails);
        } else {
            log.debug("Next hearing date feature not enabled");
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
