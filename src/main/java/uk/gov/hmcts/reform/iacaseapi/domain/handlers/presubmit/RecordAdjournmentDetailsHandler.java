package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESERVE_OR_EXCLUDE_JUDGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay.ON_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.adjournedBeforeHearingDay;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.adjournedOnHearingDay;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.relistCaseImmediately;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AdjournmentDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AutoRequestHearingService;

@Component
@RequiredArgsConstructor
public class RecordAdjournmentDetailsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final AutoRequestHearingService autoRequestHearingService;

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == ABOUT_TO_SUBMIT
               && callback.getEvent() == RECORD_ADJOURNMENT_DETAILS;
    }


    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        addressStaleOptionalInformation(asylumCase);
        preserveAdjournmentDetailsHistory(asylumCase);
        buildCurrentAdjournmentDetail(asylumCase);
        setFloatSuitability(asylumCase);

        return new PreSubmitCallbackResponse<>(makeAutoHearingRequest(callback));
    }

    private void preserveAdjournmentDetailsHistory(AsylumCase asylumCase) {

        asylumCase.read(CURRENT_ADJOURNMENT_DETAIL, AdjournmentDetail.class).ifPresent(detail -> {
            Optional<List<IdValue<AdjournmentDetail>>> optionalPreviousAdjournmentDetails = asylumCase
                    .read(PREVIOUS_ADJOURNMENT_DETAILS);
            List<IdValue<AdjournmentDetail>> previousAdjournmentDetails = optionalPreviousAdjournmentDetails
                    .orElseGet(ArrayList::new);
            previousAdjournmentDetails.add(new IdValue<>(String.valueOf(previousAdjournmentDetails.size()), detail));
            asylumCase.write(PREVIOUS_ADJOURNMENT_DETAILS, previousAdjournmentDetails);
        });

    }

    private void buildCurrentAdjournmentDetail(AsylumCase asylumCase) {

        String adjournmentDetailsHearing = asylumCase.read(ADJOURNMENT_DETAILS_HEARING, DynamicList.class)
                .map(dynamicList -> dynamicList.getValue().getLabel()).orElse("");

        if (!adjournmentDetailsHearing.isBlank()) {

            asylumCase.write(CURRENT_ADJOURNMENT_DETAIL, AdjournmentDetail.builder()
                .adjournmentDetailsHearing(adjournmentDetailsHearing)
                .hearingAdjournmentWhen(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class)
                        .map(HearingAdjournmentDay::getValue).orElse(""))
                .hearingAdjournmentDecisionParty(asylumCase.read(HEARING_ADJOURNMENT_DECISION_PARTY, String.class)
                        .orElse(""))
                .hearingAdjournmentDecisionPartyName(asylumCase.read(HEARING_ADJOURNMENT_DECISION_PARTY_NAME, String.class)
                        .orElse(""))
                .hearingAdjournmentRequestingParty(asylumCase.read(HEARING_ADJOURNMENT_REQUESTING_PARTY, String.class)
                        .orElse(""))
                .anyAdditionalAdjournmentInfo(asylumCase.read(ANY_ADDITIONAL_ADJOURNMENT_INFO, YesOrNo.class)
                        .map(YesOrNo::toString).orElse(""))
                .additionalAdjournmentInfo(asylumCase.read(ADDITIONAL_ADJOURNMENT_INFO, String.class).orElse(""))
                .relistCaseImmediately(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class)
                        .map(YesOrNo::toString).orElse(""))
                .nextHearingFormat(asylumCase.read(NEXT_HEARING_FORMAT, DynamicList.class)
                        .map(dynamicList -> dynamicList.getValue().getLabel()).orElse(""))
                .nextHearingVenue(asylumCase.read(NEXT_HEARING_VENUE, DynamicList.class)
                        .map(dynamicList -> dynamicList.getValue().getLabel()).orElse(""))
                .nextHearingDuration(asylumCase.read(NEXT_HEARING_DURATION, String.class).orElse(""))
                .nextHearingDate(asylumCase.read(NEXT_HEARING_DATE, String.class).orElse(""))
                .nextHearingDateFixed(asylumCase.read(NEXT_HEARING_DATE_FIXED, String.class).orElse(""))
                .nextHearingDateRangeEarliest(asylumCase.read(NEXT_HEARING_DATE_RANGE_EARLIEST, String.class)
                        .orElse(""))
                .nextHearingDateRangeLatest(asylumCase.read(NEXT_HEARING_DATE_RANGE_LATEST, String.class)
                        .orElse(""))
                .shouldReserveOrExcludeJudge(asylumCase.read(SHOULD_RESERVE_OR_EXCLUDE_JUDGE, YesOrNo.class)
                        .map(YesOrNo::toString).orElse(""))
                .reserveOrExcludeJudge(asylumCase.read(RESERVE_OR_EXCLUDE_JUDGE, String.class).orElse(""))
                .build());

        }
    }

    private void setFloatSuitability(AsylumCase asylumCase) {
        Optional<HearingAdjournmentDay> optionalHearingAdjournmentDay = asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class);
        Optional<YesOrNo> optionalIsAppealSuitableToFloat = asylumCase.read(IS_APPEAL_SUITABLE_TO_FLOAT, YesOrNo.class);

        if (optionalHearingAdjournmentDay.isPresent() &&
                optionalHearingAdjournmentDay.get().equals(ON_HEARING_DATE) &&
                optionalIsAppealSuitableToFloat.isPresent() &&
                optionalIsAppealSuitableToFloat.get().equals(YES)) {
            asylumCase.write(IS_APPEAL_SUITABLE_TO_FLOAT, NO);
        }
    }

    private void addressStaleOptionalInformation(AsylumCase asylumCase) {
        boolean anyAdditionalInfo = asylumCase.read(ANY_ADDITIONAL_ADJOURNMENT_INFO, YesOrNo.class)
            .map(additionalInfo -> YES == additionalInfo).orElse(false);
        if (!anyAdditionalInfo) {
            asylumCase.clear(ADDITIONAL_ADJOURNMENT_INFO);
        }

        boolean reserveOrExcludeJudge = asylumCase.read(SHOULD_RESERVE_OR_EXCLUDE_JUDGE, YesOrNo.class)
            .map(reserveOrExclude -> YES == reserveOrExclude).orElse(false);
        if (!reserveOrExcludeJudge) {
            asylumCase.clear(RESERVE_OR_EXCLUDE_JUDGE);
        }
    }

    private AsylumCase makeAutoHearingRequest(Callback<AsylumCase> callback) {

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        boolean relistImmediately = relistCaseImmediately(asylumCase, true);

        boolean canAutoCreate = relistImmediately && adjournedOnHearingDay(asylumCase);

        if (autoRequestHearingService.shouldAutoRequestHearing(asylumCase, canAutoCreate)) {

            asylumCase = autoRequestHearingService
                .autoCreateHearing(callback);
        } else if (relistImmediately && adjournedBeforeHearingDay(asylumCase)) {

            asylumCase = autoRequestHearingService
                .autoUpdateHearing(callback);
        } else if (!relistImmediately && adjournedBeforeHearingDay(asylumCase)) {

            asylumCase = autoRequestHearingService
                .autoCancelHearing(callback);
        }

        return asylumCase;
    }

}
