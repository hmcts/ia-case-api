package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE_RANGE_EARLIEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE_RANGE_LATEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DURATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_FORMAT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_LOCATION;

@Component
public class RecordAdjournmentDetailsMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public static final String NEXT_HEARING_DATE_CHOOSE_DATE_RANGE = "ChooseADateRange";
    public static final String NEXT_HEARING_DATE_RANGE_ERROR_MESSAGE = "You must provide one of the earliest or latest hearing " +
            "date";

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
        asylumCase.read(HEARING_CHANNEL, DynamicList.class)
                .ifPresent(hearingChannel -> {
                    Optional<DynamicList> nextHearingFormat = asylumCase.read(NEXT_HEARING_FORMAT, DynamicList.class);
                    nextHearingFormat.ifPresent(it -> it.setValue(hearingChannel.getValue()));
                    asylumCase.write(NEXT_HEARING_FORMAT, nextHearingFormat.get());
                });

        asylumCase.read(LIST_CASE_HEARING_LENGTH, String.class)
                .ifPresent(hearingLength -> asylumCase.write(NEXT_HEARING_DURATION, hearingLength));
        asylumCase.read(HEARING_CENTRE, HearingCentre.class)
                .ifPresent(hearingCentre -> asylumCase.write(NEXT_HEARING_LOCATION, hearingCentre));
        return validateHearingDateRange(asylumCase);
    }

    private PreSubmitCallbackResponse<AsylumCase> validateHearingDateRange(AsylumCase asylumCase) {
        PreSubmitCallbackResponse<AsylumCase> preSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
        asylumCase.read(NEXT_HEARING_DATE, String.class).ifPresent(nextHearingDate -> {
            if (nextHearingDate.equals(NEXT_HEARING_DATE_CHOOSE_DATE_RANGE)) {
                if (asylumCase.read(NEXT_HEARING_DATE_RANGE_EARLIEST, String.class).isEmpty()
                        && asylumCase.read(NEXT_HEARING_DATE_RANGE_LATEST, String.class).isEmpty()) {
                    preSubmitCallbackResponse.addError(NEXT_HEARING_DATE_RANGE_ERROR_MESSAGE);
                }
            }
        });
        return preSubmitCallbackResponse;
    }
}
