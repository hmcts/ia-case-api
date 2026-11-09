package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.HearingPause;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CommonRefDataDynamicListProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PAUSED_TIME_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PAUSES;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordAttendeesAndDurationMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private static final String SELECT_ATTENDEES_AND_DURATION_HEARING_LIST_PAGE_ID =
            "selectHearingRecordAttendeesAndDuration";
    private static final String TIMES_PAGE_ID = "timesRecordAttendeesAndDuration";
    private static final int MAX_PAUSES = 1;

    private final CommonRefDataDynamicListProvider provider;
    private final LocationRefDataService locationRefDataService;
    private final IaHearingsApiService iaHearingsApiService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        log.info("handling event {}, callbackStage {}", callback.getEvent(), callbackStage);

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && callback.getEvent() == Event.RECORD_ATTENDEES_AND_DURATION;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        log.info("Handling callback for RecordAttendeesAndDurationMidEvent, page {}", callback.getPageId());

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        switch (callback.getPageId()) {
            case SELECT_ATTENDEES_AND_DURATION_HEARING_LIST_PAGE_ID -> {
                log.info("selecting attendees and duration hearing");
                asylumCase = iaHearingsApiService.midEvent(callback);

                prePopulateActualHearingCancellationReason(asylumCase);
                prePopulateActualHearingPartHeardReason(asylumCase);
                response = new PreSubmitCallbackResponse<>(asylumCase);
            }
            case TIMES_PAGE_ID -> validatePauses(asylumCase, response);
            default -> { }
        }

        return response;
    }

    private void validatePauses(AsylumCase asylumCase, PreSubmitCallbackResponse<AsylumCase> response) {
        boolean pausedTimeRequired = asylumCase.read(ATTENDEES_AND_DURATION_PAUSED_TIME_REQUIRED, YesOrNo.class)
            .map(YesOrNo.YES::equals)
            .orElse(false);
        if (!pausedTimeRequired) {
            return;
        }

        Optional<List<IdValue<HearingPause>>> pausesOpt = asylumCase.read(ATTENDEES_AND_DURATION_PAUSES);
        if (pausesOpt.orElse(Collections.emptyList()).size() > MAX_PAUSES) {
            response.addError("You can only add one pause");
        }
    }

    private void prePopulateActualHearingCancellationReason(AsylumCase asylumCase) {
        DynamicList hearingCancellationReason =
            asylumCase.read(ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON, DynamicList.class)
                .orElse(null);
        if (hearingCancellationReason == null) {
            hearingCancellationReason = provider.provideActualCancellationReasonCodes();
        } else {
            hearingCancellationReason = new DynamicList(
                hearingCancellationReason.getValue(),
                provider.provideActualCancellationReasonCodes().getListItems()
            );
        }

        log.info("hearingCancellationReason = {}", hearingCancellationReason);
        asylumCase.write(ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON, hearingCancellationReason);
    }

    private void prePopulateActualHearingPartHeardReason(AsylumCase asylumCase) {
        DynamicList hearingPartHeardReason =
            asylumCase.read(ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON, DynamicList.class)
                .orElse(null);
        if (hearingPartHeardReason == null) {
            hearingPartHeardReason = provider.provideActualPartHeardReasonCodes();
        } else {
            hearingPartHeardReason = new DynamicList(
                hearingPartHeardReason.getValue(),
                provider.provideActualPartHeardReasonCodes().getListItems()
            );
        }

        log.info("hearingPartHeardReason = {}", hearingPartHeardReason);
        asylumCase.write(ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON, hearingPartHeardReason);
    }
}
