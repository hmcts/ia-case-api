package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OTHER_DECISION_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;

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

    public static String getAppellantFullName(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class).orElseGet(() -> {
            final String appellantGivenNames = asylumCase
                    .read(APPELLANT_GIVEN_NAMES, String.class)
                    .orElseThrow(() -> new IllegalStateException("Appellant given names required"));
            final String appellantFamilyName = asylumCase
                    .read(APPELLANT_FAMILY_NAME, String.class)
                    .orElseThrow(() -> new IllegalStateException("Appellant family name required"));
            return appellantGivenNames + " " + appellantFamilyName;
        });
    }

    private static Optional<String> formatHearingAdjustmentResponse(
            AsylumCase asylumCase,
            AsylumCaseFieldDefinition responseDefinition,
            AsylumCaseFieldDefinition decisionDefinition) {
        String response = asylumCase.read(responseDefinition, String.class).orElse(null);
        String decision = asylumCase.read(decisionDefinition, String.class).orElse(null);

        return !(response == null || decision == null) ? Optional.of(decision + " - " + response) : Optional.empty();
    }

    public static void checkAndUpdateAutoHearingRequestEnabled(LocationBasedFeatureToggler locationBasedFeatureToggler, AsylumCase asylumCase) {
        if (locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase) == YES) {
            asylumCase.write(AUTO_HEARING_REQUEST_ENABLED, YES);
        } else {
            asylumCase.write(AUTO_HEARING_REQUEST_ENABLED, NO);
        }
    }
}
