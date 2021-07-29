package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class AppellantNameForDisplayFormatter implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        Optional<JourneyType> journeyTypeOptional = callback.getCaseDetails().getCaseData().read(JOURNEY_TYPE);
        boolean isAipJourney = journeyTypeOptional.map(journeyType -> journeyType == JourneyType.AIP).orElse(false);
        Event event = callback.getEvent();
        boolean isStartOrEditAipEvent = isAipJourney && (event == Event.START_APPEAL || event == Event.EDIT_APPEAL);
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && !isStartOrEditAipEvent && event != Event.ADMIN_CASE_UPDATE;
    }

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

        final String appellantGivenNames =
                asylumCase
                        .read(APPELLANT_GIVEN_NAMES, String.class)
                        .orElseThrow(() -> new IllegalStateException("appellantGivenNames is not present"));

        final String appellantFamilyName =
                asylumCase
                        .read(APPELLANT_FAMILY_NAME, String.class)
                        .orElseThrow(() -> new IllegalStateException("appellantFamilyName is not present"));

        String appellantNameForDisplay = appellantGivenNames + " " + appellantFamilyName;

        asylumCase.write(
                APPELLANT_NAME_FOR_DISPLAY,
                appellantNameForDisplay.replaceAll("\\s+", " ").trim()
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
