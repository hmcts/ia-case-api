package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_EARLIEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_LATEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_YES_NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_UPDATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;

@Component
@Slf4j
public class HearingsUpdateHearingRequest implements PreSubmitCallbackHandler<AsylumCase> {

    public static final String NO_HEARINGS_ERROR_MESSAGE =
        "You've made an invalid request. You must request a substantive hearing before you can update a hearing.";
    private static final String UPDATE_HEARING_DATE_PAGE_ID = "updateHearingDate";
    private static final String UPDATE_HEARING_LIST_PAGE_ID = "updateHearingList";
    private static final String COMPLIANT_DATE_RANGE_NEEDED = "Earliest hearing date or Latest hearing date required";
    private static final String CHOOSE_A_DATE_RANGE = "ChooseADateRange";

    private IaHearingsApiService iaHearingsApiService;

    public HearingsUpdateHearingRequest(
            IaHearingsApiService iaHearingsApiService
    ) {
        this.iaHearingsApiService = iaHearingsApiService;
    }

    @Override
    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == ABOUT_TO_START
                || callbackStage == MID_EVENT)
                && Objects.equals(Event.UPDATE_HEARING_REQUEST, callback.getEvent());
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

        String pageId = callback.getPageId();

        if (StringUtils.equals(pageId, UPDATE_HEARING_LIST_PAGE_ID)) {
            if (StringUtils.equals(pageId, UPDATE_HEARING_LIST_PAGE_ID)
                && asylumCase.read(CHANGE_HEARINGS).isEmpty()) {

                asylumCase = getHearings(callback);

                if (hasNoHearings(asylumCase)) {
                    PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
                    response.addError(NO_HEARINGS_ERROR_MESSAGE);
                    return response;
                }
            } else {
                asylumCase = getHearingDetails(callback);
            }
        }

        if (StringUtils.equals(pageId, UPDATE_HEARING_DATE_PAGE_ID)
            && isNeededDateRangeNonCompliant(asylumCase)) {

            PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
            response.addError(COMPLIANT_DATE_RANGE_NEEDED);
            return response;
        }

        asylumCase.clear(MANUAL_UPDATE_HEARING_REQUIRED);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean hasNoHearings(AsylumCase asylumCase) {
        Optional<DynamicList> hearings = asylumCase.read(CHANGE_HEARINGS, DynamicList.class);

        if (hearings.isEmpty()) {
            return true;
        } else {
            return hearings.get().getListItems().isEmpty();
        }
    }

    private AsylumCase getHearings(Callback<AsylumCase> callback) {
        return iaHearingsApiService.aboutToStart(callback);
    }

    private AsylumCase getHearingDetails(Callback<AsylumCase> callback) {
        return iaHearingsApiService.midEvent(callback);
    }

    private boolean isNeededDateRangeNonCompliant(AsylumCase asylumCase) {
        return asylumCase.read(CHANGE_HEARING_DATE_YES_NO, String.class)
                   .map(yesOrNo -> StringUtils.equals("yes", yesOrNo)).orElse(false)
               && asylumCase.read(CHANGE_HEARING_DATE_TYPE, String.class)
                   .map(type -> type.equals(CHOOSE_A_DATE_RANGE)).orElse(false)
               && asylumCase.read(CHANGE_HEARING_DATE_RANGE_EARLIEST, String.class).isEmpty()
               && asylumCase.read(CHANGE_HEARING_DATE_RANGE_LATEST, String.class).isEmpty();
    }
}
