package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import java.util.Objects;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_EARLIEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_LATEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_YES_NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;

@Component
@Slf4j
@RequiredArgsConstructor
public class HearingsUpdateHearingRequestMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String UPDATE_HEARING_DATE_PAGE_ID = "updateHearingDate";
    private static final String UPDATE_HEARING_LIST_PAGE_ID = "updateHearingList";
    private static final String COMPLIANT_DATE_RANGE_NEEDED = "Earliest hearing date or Latest hearing date required";
    private static final String CHOOSE_A_DATE_RANGE = "ChooseADateRange";

    private final IaHearingsApiService iaHearingsApiService;

    @Override
    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == MID_EVENT && callback.getEvent() == UPDATE_HEARING_REQUEST;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        String errorMessage = null;

        switch (callback.getPageId()) {
            case UPDATE_HEARING_LIST_PAGE_ID -> asylumCase = iaHearingsApiService.midEvent(callback);
            case UPDATE_HEARING_DATE_PAGE_ID -> errorMessage = isNeededDateRangeNonCompliant(asylumCase)
                ? COMPLIANT_DATE_RANGE_NEEDED
                : null;
            default -> { }
        }

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
        if (errorMessage != null) {
            response.addError(errorMessage);
        }

        return response;
    }

    private boolean isNeededDateRangeNonCompliant(AsylumCase asylumCase) {
        return asylumCase.read(CHANGE_HEARING_DATE_YES_NO, String.class)
                   .map(yesOrNo -> Objects.equals("yes", yesOrNo)).orElse(false)
               && asylumCase.read(CHANGE_HEARING_DATE_TYPE, String.class)
                   .map(type -> type.equals(CHOOSE_A_DATE_RANGE)).orElse(false)
               && asylumCase.read(CHANGE_HEARING_DATE_RANGE_EARLIEST, String.class).isEmpty()
               && asylumCase.read(CHANGE_HEARING_DATE_RANGE_LATEST, String.class).isEmpty();
    }
}
