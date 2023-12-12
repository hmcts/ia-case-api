package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RefDataUserService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CANCELLATION_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_RELISTED_UPDATE_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE_RANGE_EARLIEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE_RANGE_LATEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DURATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_FORMAT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RELIST_CASE_IMMEDIATELY;

@Component
@AllArgsConstructor
public class RecordAdjournmentDetailsMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {
    public static final String INITIALIZE_FIELDS_PAGE_ID = "relistCaseImmediately";
    public static final String CHECK_HEARING_DATE_PAGE_ID = "nextHearingDate";

    public static final String NEXT_HEARING_DATE_CHOOSE_DATE_RANGE = "ChooseADateRange";
    public static final String NEXT_HEARING_DATE_RANGE_ERROR_MESSAGE = "You must provide one of the earliest or latest hearing " +
            "date";
    public static final String CASE_MANAGEMENT_CANCELLATION_REASONS = "CaseManagementCancellationReasons";
    public static final String CHANGE_REASONS = "ChangeReasons";
    public static final String IS_CHILD_REQUIRED = "N";

    private final RefDataUserService refDataUserService;

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

        if (callback.getPageId().equals(INITIALIZE_FIELDS_PAGE_ID)) {
            prePopulateCancellationOrUpdateReasons(asylumCase);
            prePopulateHearingDetails(asylumCase);
        }

        return validateHearingDateRange(callback, asylumCase);
    }

    private static void prePopulateHearingDetails(AsylumCase asylumCase) {
        asylumCase.read(HEARING_CHANNEL, DynamicList.class)
                .ifPresent(hearingChannel -> {
                    Optional<DynamicList> nextHearingFormat = asylumCase.read(NEXT_HEARING_FORMAT, DynamicList.class);
                    nextHearingFormat.ifPresent(it -> it.setValue(hearingChannel.getValue()));
                    asylumCase.write(NEXT_HEARING_FORMAT, nextHearingFormat.get());
                });

        asylumCase.read(LIST_CASE_HEARING_LENGTH, String.class)
                .ifPresent(hearingLength -> asylumCase.write(NEXT_HEARING_DURATION, hearingLength));
        asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .ifPresent(hearingCentre -> asylumCase.write(NEXT_HEARING_LOCATION, hearingCentre));
    }

    private PreSubmitCallbackResponse<AsylumCase> validateHearingDateRange(Callback<AsylumCase> callback,
                                                                           AsylumCase asylumCase) {

        PreSubmitCallbackResponse<AsylumCase> preSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
        if (callback.getPageId().equals(CHECK_HEARING_DATE_PAGE_ID)) {
            asylumCase.read(NEXT_HEARING_DATE, String.class).ifPresent(nextHearingDate -> {
                if (nextHearingDate.equals(NEXT_HEARING_DATE_CHOOSE_DATE_RANGE)) {
                    if (asylumCase.read(NEXT_HEARING_DATE_RANGE_EARLIEST, String.class).isEmpty()
                            && asylumCase.read(NEXT_HEARING_DATE_RANGE_LATEST, String.class).isEmpty()) {
                        preSubmitCallbackResponse.addError(NEXT_HEARING_DATE_RANGE_ERROR_MESSAGE);
                    }
                }
            });
        }
        return preSubmitCallbackResponse;
    }

    private void prePopulateCancellationOrUpdateReasons(AsylumCase asylumCase) {
        boolean isHearingRelisted = asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class)
                .map(hearingRelisted -> Objects.equals(hearingRelisted, YesOrNo.YES)).orElse(false);

        if (isHearingRelisted) {
            populateDynamicList(asylumCase, CHANGE_REASONS, HEARING_RELISTED_UPDATE_REASON);
        } else {
            populateDynamicList(asylumCase, CASE_MANAGEMENT_CANCELLATION_REASONS, HEARING_CANCELLATION_REASON);
        }
    }

    private void populateDynamicList(AsylumCase asylumCase,
                                     String cancellationUpdateReason,
                                     AsylumCaseFieldDefinition reasonsFieldDefinition) {

        CommonDataResponse commonDataResponse = refDataUserService.retrieveCategoryValues(
                cancellationUpdateReason,
                IS_CHILD_REQUIRED
        );
        List<Value> reasons = commonDataResponse.getCategoryValues()
                .stream()
                .map(categoryValues -> new Value(categoryValues.getKey(), categoryValues.getValueEn()))
                .collect(Collectors.toList());

        asylumCase.write(reasonsFieldDefinition, new DynamicList(new Value("", ""), reasons));
    }
}
