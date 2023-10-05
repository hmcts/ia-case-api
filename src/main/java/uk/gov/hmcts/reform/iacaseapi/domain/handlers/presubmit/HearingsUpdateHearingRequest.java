package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseCallbackApiDelegator;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_LOCATION_CHANGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_LOCATION_VALUE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_HEARINGS;

@Component
@Slf4j
public class HearingsUpdateHearingRequest implements PreSubmitCallbackHandler<AsylumCase> {

    private final String hearingsApiEndpoint;
    private final String aboutToStartPath;
    private final String midEventPath;

    AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;

    public HearingsUpdateHearingRequest(
            AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator,
            @Value("${hearingsApi.endpoint}") String hearingsApiEndpoint,
            @Value("${hearingsApi.aboutToStartPath}") String aboutToStartPath,
            @Value("${hearingsApi.midEventPath}") String midEventPath
    ) {
        this.asylumCaseCallbackApiDelegator = asylumCaseCallbackApiDelegator;
        this.hearingsApiEndpoint = hearingsApiEndpoint;
        this.aboutToStartPath = aboutToStartPath;
        this.midEventPath = midEventPath;
    }

    @Override
    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                || callbackStage == PreSubmitCallbackStage.MID_EVENT)
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

        AsylumCase asylumCase;

        if (callback.getCaseDetails().getCaseData().read(UPDATE_HEARINGS).isEmpty()) {
            asylumCase = getHearings(callback);
        } else {
            asylumCase = getHearingDetails(callback);
            setHearingLocationDetails(asylumCase);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private AsylumCase getHearings(Callback<AsylumCase> callback) {
        return asylumCaseCallbackApiDelegator.delegate(callback, hearingsApiEndpoint + aboutToStartPath);
    }

    private AsylumCase getHearingDetails(Callback<AsylumCase> callback) {
        return asylumCaseCallbackApiDelegator.delegate(callback, hearingsApiEndpoint + midEventPath);
    }

    private static void setHearingLocationDetails(AsylumCase asylumCase) {
        Optional<String> hearingLocation = asylumCase.read(HEARING_LOCATION_VALUE);
        if (hearingLocation.isPresent()) {
            String hearingCenterValue = HearingCentre.getValueByEpimsId(hearingLocation.get());
            asylumCase.write(HEARING_LOCATION_VALUE, hearingCenterValue);
            DynamicList locationsList = new DynamicList(
                    new uk.gov.hmcts.reform.iacaseapi.domain.entities.Value(hearingLocation.get(), hearingCenterValue),
                    HearingCentre.epimsIdMapping
                            .values()
                            .stream()
                            .map(hearingCentre -> new uk.gov.hmcts.reform.iacaseapi.domain.entities.Value(hearingCentre.getEpimsId(), hearingCentre.getValue()))
                            .collect(Collectors.toList())
            );
            asylumCase.write(HEARING_LOCATION_CHANGE, locationsList);
        }
    }
}
