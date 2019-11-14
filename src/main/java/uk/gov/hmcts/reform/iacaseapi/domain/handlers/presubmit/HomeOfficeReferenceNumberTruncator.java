package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class HomeOfficeReferenceNumberTruncator implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        Optional<JourneyType> journeyTypeOptional = callback.getCaseDetails().getCaseData().read(JOURNEY_TYPE);
        boolean isAipJourney = journeyTypeOptional.map(journeyType -> journeyType == JourneyType.AIP).orElse(false);

        return !isAipJourney && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.START_APPEAL || callback.getEvent() == Event.EDIT_APPEAL);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Optional<String> maybeHomeOfficeReferenceNumber =
                asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER);

        String homeOfficeReferenceNumber =
                maybeHomeOfficeReferenceNumber.orElseThrow(() -> new RequiredFieldMissingException("homeOfficeReferenceNumber is not present"));

        if (homeOfficeReferenceNumber.contains("/") || homeOfficeReferenceNumber.length() > 8) {
            String truncatedReferenceNumber = homeOfficeReferenceNumber.split("/")[0];

            if (truncatedReferenceNumber.length() > 8) {
                truncatedReferenceNumber = truncatedReferenceNumber.substring(0, 8);
            }

            asylumCase.write(HOME_OFFICE_REFERENCE_NUMBER, truncatedReferenceNumber);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
