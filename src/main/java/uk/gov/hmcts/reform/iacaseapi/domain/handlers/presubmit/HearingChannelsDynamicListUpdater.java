package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_FORMAT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RefDataUserService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;

@Component
public class HearingChannelsDynamicListUpdater implements PreSubmitCallbackHandler<AsylumCase> {
    RefDataUserService refDataUserService;

    public HearingChannelsDynamicListUpdater(RefDataUserService refDataUserService) {
        this.refDataUserService = refDataUserService;
    }

    public static final String HEARING_CHANNEL_CATEGORY = "HearingChannel";
    public static final String IS_CHILD_REQUIRED = "N";
    public static final String INITIALIZE_FIELDS_PAGE_ID = "relistCaseImmediately";

    @Override
    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT && List.of(
                Event.UPDATE_HEARING_ADJUSTMENTS,
                Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS,
                Event.REVIEW_HEARING_REQUIREMENTS,
                RECORD_ADJOURNMENT_DETAILS).contains(callback.getEvent());
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (callback.getEvent() == RECORD_ADJOURNMENT_DETAILS &&
                callback.getPageId().equals(INITIALIZE_FIELDS_PAGE_ID)) {
            asylumCase.write(NEXT_HEARING_FORMAT, populateDynamicList());
        } else {
            asylumCase.write(HEARING_CHANNEL, populateDynamicList());
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private DynamicList populateDynamicList() {

        try {
            CommonDataResponse commonDataResponse = refDataUserService.retrieveCategoryValues(
                    HEARING_CHANNEL_CATEGORY,
                    IS_CHILD_REQUIRED
            );

            List<CategoryValues> hearingChannels = refDataUserService
                    .filterCategoryValuesByCategoryIdWithActiveFlag(commonDataResponse, HEARING_CHANNEL_CATEGORY);

            return new DynamicList(new Value("", ""),
                    refDataUserService.mapCategoryValuesToDynamicListValues(hearingChannels));

        } catch (Exception e) {
            throw new RuntimeException("Couldn't read response by RefData service for HearingChannel(s)", e);
        }
    }
}
