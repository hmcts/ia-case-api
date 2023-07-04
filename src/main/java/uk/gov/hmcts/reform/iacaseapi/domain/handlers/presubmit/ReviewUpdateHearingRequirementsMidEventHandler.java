package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL_IN_ADJUSTMENT;

import java.util.List;
import java.util.Objects;

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
public class ReviewUpdateHearingRequirementsMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {
    RefDataUserService refDataUserService;

    public ReviewUpdateHearingRequirementsMidEventHandler(RefDataUserService refDataUserService) {
        this.refDataUserService = refDataUserService;
    }

    public static final String HEARING_CHANNEL = "HearingChannel";
    public static final String IS_CHILD_REQUIRED = "N";
//    public static final String REVIEW_UPDATE_HEARING_REQUIREMENTS_PAGE_ID = "reviewUpdateHearingRequirements";

    @Override
    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && callback.getEvent() == Event.UPDATE_HEARING_ADJUSTMENTS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        String pageId = callback.getPageId();

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

//        if (Objects.equals(pageId, REVIEW_UPDATE_HEARING_REQUIREMENTS_PAGE_ID)
//                && callback.getEvent() == Event.UPDATE_HEARING_ADJUSTMENTS) {
            System.out.println("######ReviewUpdateHearingRequirementsMidEventHandler");
            populateDynamicList(asylumCase);
//        }

        return response;
    }

    private AsylumCase populateDynamicList(AsylumCase asylumCase) {
        List<CategoryValues> hearingChannels;
        DynamicList dynamicListOfHearingChannel;

        try {
            CommonDataResponse commonDataResponse = refDataUserService.retrieveCategoryValues(
                    HEARING_CHANNEL,
                    IS_CHILD_REQUIRED
            );
            System.out.println("######commonDataResponse.size:"+commonDataResponse.getCategoryValues().size());

            hearingChannels = refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, HEARING_CHANNEL);

            dynamicListOfHearingChannel = new DynamicList(new Value("", ""),
                    refDataUserService.mapCategoryValuesToDynamicListValues(hearingChannels));

            System.out.println("######hearingChannels.size:"+hearingChannels.size());
            System.out.println("######dynamicListOfHearingChannel.getListItems().size:"+dynamicListOfHearingChannel.getListItems().size());


        } catch (Exception e) {
            throw new RuntimeException("Couldn't read response by RefData service for HearingChannel(s)", e);
        }

//        InterpreterLanguage interpreterLanguageObject = new InterpreterLanguage(
//                "",
//                dynamicListOfLanguages,
//                "",
//                Collections.emptyList(),
//                "");
//
//        List<IdValue<InterpreterLanguage>> interpreterLanguageCollection = List.of(
//                new IdValue<>("1", interpreterLanguageObject)
//        );
//
//        asylumCase.write(INTERPRETER_LANGUAGE, interpreterLanguageCollection);
        asylumCase.write(HEARING_CHANNEL_IN_ADJUSTMENT, dynamicListOfHearingChannel);

        return asylumCase;
    }
}
