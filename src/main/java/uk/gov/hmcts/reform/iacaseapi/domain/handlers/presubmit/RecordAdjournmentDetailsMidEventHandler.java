package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_REASON_TO_CANCEL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_REASON_TO_UPDATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE_RANGE_EARLIEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE_RANGE_LATEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DURATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_FORMAT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_VENUE;

import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CommonRefDataDynamicListProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

@Component
@RequiredArgsConstructor
public class RecordAdjournmentDetailsMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {
    public static final String INITIALIZE_FIELDS_PAGE_ID = "relistCaseImmediately";
    public static final String CHECK_HEARING_DATE_PAGE_ID = "nextHearingDate";

    public static final String NEXT_HEARING_DATE_CHOOSE_DATE_RANGE = "ChooseADateRange";
    public static final String NEXT_HEARING_DATE_RANGE_ERROR_MESSAGE = "You must provide one of the earliest or latest hearing " +
            "date";

    private final CommonRefDataDynamicListProvider provider;
    private final LocationRefDataService locationRefDataService;
    private final IaHearingsApiService iaHearingsApiService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && callback.getEvent() == Event.RECORD_ADJOURNMENT_DETAILS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        switch (callback.getPageId()) {
            case CHECK_HEARING_DATE_PAGE_ID -> {
                if (isDateRange(asylumCase) && !isValidDateRange(asylumCase)) {
                    PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
                    response.addError(NEXT_HEARING_DATE_RANGE_ERROR_MESSAGE);
                    return response;
                }
            }
            case INITIALIZE_FIELDS_PAGE_ID -> {
                asylumCase = prePopulateHearingVenue(callback);
                prePopulateCancellationOrUpdateReasons(asylumCase);
                prePopulateNextHearingFormat(asylumCase);
                prePopulateNextHearingDuration(asylumCase);
            }
            default -> { }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void prePopulateNextHearingFormat(AsylumCase asylumCase) {

        DynamicList hearingChannel = asylumCase.read(HEARING_CHANNEL, DynamicList.class).orElse(null);
        if (hearingChannel == null) {
            hearingChannel = provider.provideHearingChannels();
        } else if (hearingChannel.getListItems() == null || hearingChannel.getListItems().isEmpty()) {
            hearingChannel = new DynamicList(hearingChannel.getValue(), provider.provideHearingChannels().getListItems());
        }

        asylumCase.write(NEXT_HEARING_FORMAT, hearingChannel);
    }

    private void prePopulateNextHearingDuration(AsylumCase asylumCase) {

        asylumCase.read(LIST_CASE_HEARING_LENGTH, String.class)
            .ifPresent(hearingLength -> asylumCase.write(NEXT_HEARING_DURATION, hearingLength));
    }

    private AsylumCase prePopulateHearingVenue(Callback<AsylumCase> callback) {
        DynamicList refDataHearingLocationList = locationRefDataService.getHearingLocationsDynamicList();
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<HearingCentre> hearingCentre = asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        if (hearingCentre.isPresent()) {
            Value hearingLocationValue = refDataHearingLocationList.getListItems().stream()
                .filter(location -> Objects.equals(location.getCode(), hearingCentre.get().getEpimsId()))
                .findAny().orElseGet(() -> new Value("", ""));
            refDataHearingLocationList.setValue(hearingLocationValue);
            asylumCase.write(NEXT_HEARING_VENUE, refDataHearingLocationList);
        } else {
            asylumCase.write(NEXT_HEARING_VENUE, refDataHearingLocationList);
            asylumCase = iaHearingsApiService.midEvent(callback);
        }

        return asylumCase;
    }

    private boolean isValidDateRange(AsylumCase asylumCase) {

        return asylumCase.read(NEXT_HEARING_DATE_RANGE_EARLIEST, String.class).isPresent()
               || asylumCase.read(NEXT_HEARING_DATE_RANGE_LATEST, String.class).isPresent();
    }

    private boolean isDateRange(AsylumCase asylumCase) {

        return asylumCase.read(NEXT_HEARING_DATE, String.class)
            .map(NEXT_HEARING_DATE_CHOOSE_DATE_RANGE::equals)
            .orElse(false);
    }

    private void prePopulateCancellationOrUpdateReasons(AsylumCase asylumCase) {

        if (HandlerUtils.relistCaseImmediately(asylumCase, false)) {
            asylumCase.write(HEARING_REASON_TO_UPDATE, provider.provideChangeReasons());
        } else {
            asylumCase.write(HEARING_REASON_TO_CANCEL, provider.provideCaseManagementCancellationReasons());
        }
    }
}
