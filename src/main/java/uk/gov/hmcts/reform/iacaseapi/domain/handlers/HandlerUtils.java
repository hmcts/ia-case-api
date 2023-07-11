package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OTHER_DECISION_FOR_DISPLAY;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;

public class HandlerUtils {

    private HandlerUtils() {
    }

    public static boolean isAipJourney(AsylumCase asylumCase) {
        return asylumCase.read(JOURNEY_TYPE, JourneyType.class)
            .map(journeyType -> journeyType == JourneyType.AIP)
            .orElse(false);
    }

    public static boolean isRepJourney(AsylumCase asylumCase) {
        return asylumCase.read(JOURNEY_TYPE, JourneyType.class)
            .map(journeyType -> journeyType == JourneyType.REP)
            .orElse(true);
    }

    public static boolean isRepToAipJourney(AsylumCase asylumCase) {
        return (asylumCase.read(PREV_JOURNEY_TYPE, JourneyType.class).orElse(null) == JourneyType.REP)
            && isAipJourney(asylumCase);
    }

    public static boolean isAipToRepJourney(AsylumCase asylumCase) {
        return (asylumCase.read(PREV_JOURNEY_TYPE, JourneyType.class).orElse(null) == JourneyType.AIP)
            && isRepJourney(asylumCase);
    }

    public static void formatHearingAdjustmentResponses(AsylumCase asylumCase) {
        formatHearingAdjustmentResponse(asylumCase, VULNERABILITIES_TRIBUNAL_RESPONSE, IS_VULNERABILITIES_ALLOWED)
                .ifPresent(response -> asylumCase.write(VULNERABILITIES_DECISION_FOR_DISPLAY, response));
        formatHearingAdjustmentResponse(asylumCase, REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, IS_REMOTE_HEARING_ALLOWED)
                .ifPresent(response -> asylumCase.write(REMOTE_HEARING_DECISION_FOR_DISPLAY, response));
        formatHearingAdjustmentResponse(asylumCase, MULTIMEDIA_TRIBUNAL_RESPONSE, IS_MULTIMEDIA_ALLOWED)
                .ifPresent(response -> asylumCase.write(MULTIMEDIA_DECISION_FOR_DISPLAY, response));
        formatHearingAdjustmentResponse(asylumCase, SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, IS_SINGLE_SEX_COURT_ALLOWED)
                .ifPresent(response -> asylumCase.write(SINGLE_SEX_COURT_DECISION_FOR_DISPLAY, response));
        formatHearingAdjustmentResponse(asylumCase, IN_CAMERA_COURT_TRIBUNAL_RESPONSE, IS_IN_CAMERA_COURT_ALLOWED)
                .ifPresent(response -> asylumCase.write(IN_CAMERA_COURT_DECISION_FOR_DISPLAY, response));
        formatHearingAdjustmentResponse(asylumCase, ADDITIONAL_TRIBUNAL_RESPONSE, IS_ADDITIONAL_ADJUSTMENTS_ALLOWED)
                .ifPresent(response -> asylumCase.write(OTHER_DECISION_FOR_DISPLAY, response));
    }

    private static Optional<String> formatHearingAdjustmentResponse(
            AsylumCase asylumCase,
            AsylumCaseFieldDefinition responseDefinition,
            AsylumCaseFieldDefinition decisionDefinition) {
        String response = asylumCase.read(responseDefinition, String.class).orElse(null);
        String decision = asylumCase.read(decisionDefinition, String.class).orElse(null);

        return !(response == null || decision == null) ? Optional.of(decision + " - " + response) : Optional.empty();
    }
}
