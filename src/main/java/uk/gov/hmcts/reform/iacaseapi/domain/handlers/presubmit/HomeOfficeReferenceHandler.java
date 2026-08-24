package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HomeOfficeApiResponseStatusType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeReferenceService;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.*;

@Slf4j
@Component
@ConditionalOnProperty(
    name = "app.home-office-validation.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class HomeOfficeReferenceHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final HomeOfficeReferenceService homeOfficeReferenceService;

    public HomeOfficeReferenceHandler(HomeOfficeReferenceService homeOfficeReferenceService) {
        this.homeOfficeReferenceService = homeOfficeReferenceService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
            && List.of(Event.START_APPEAL, Event.EDIT_APPEAL, Event.EDIT_APPEAL_AFTER_SUBMIT).contains(callback.getEvent())
            && List.of(
                "homeOfficeReferenceNumber", "oocHomeOfficeReferenceNumber", "appellantBasicDetails", // ExUI pages
                "cuiHomeOfficeReferenceNumber", "cuiGwfReferenceNumber", "cuiAppellantName", "cuiAppellantDob") // CUI pages
            .contains(callback.getPageId());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        // Retrieve the UAN or GWF from the case record
        String homeOfficeReferenceNumber = HandlerUtils.getUanOrGwf(asylumCase);
        if (homeOfficeReferenceNumber.isEmpty()) {
            throw new IllegalStateException("homeOfficeReferenceNumber and gwfReferenceNumber are both missing - one or other is needed");
        }

        String pageId = callback.getPageId();

        try {
            return switch (pageId) {
                case "homeOfficeReferenceNumber", "oocHomeOfficeReferenceNumber", "cuiHomeOfficeReferenceNumber",
                     "cuiGwfReferenceNumber" -> {
                    // First of all, remove any trace of a previous call to the Home Office validation API,
                    // in order to force a fresh request (in the event that we have changed the HO reference number, say)
                    HandlerUtils.removeValidationFields(asylumCase);
                    yield validateHomeOfficeReference(callback, asylumCase, homeOfficeReferenceNumber, homeOfficeReferenceService);
                }

                case "appellantBasicDetails", "cuiAppellantDob" -> {
                    boolean isCUICallback = pageId.contains("cui");
                    yield validateNameAndDateOfBirth(callback, asylumCase, homeOfficeReferenceNumber, isCUICallback, homeOfficeReferenceService);
                }

                case "cuiAppellantName" -> validateName(callback, asylumCase, homeOfficeReferenceNumber,  homeOfficeReferenceService);

                default -> new PreSubmitCallbackResponse<>(asylumCase);
            };
        } catch (Exception ex) {
            String logMessage = "Could not validate information from Home Office asylum (etc.) application with Home Office reference "
                + homeOfficeReferenceNumber + ".  The exception message was:\n\n{}"
                + "\n\nSee the corresponding logs in ia-home-office-integration-api for more details.";
            log.error(logMessage, ex.getMessage());

            PreSubmitCallbackResponse<AsylumCase> response =
                new PreSubmitCallbackResponse<>(asylumCase);

            response.addError(HomeOfficeApiResponseStatusType.UNKNOWN.getUserFacingErrorText(homeOfficeReferenceNumber));
            return response;
        }
    }
}
