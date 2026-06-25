package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HomeOfficeApiResponseStatusType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeApi;

@Slf4j
@Component
public class AppealSubmittedNotifyHomeOfficeHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String SUPPRESSION_LOG_FIELDS_NEW = "event: {}, "
                                                         + "CCD case ID: {}, "
                                                         + "HMCTS appeal ref: {}, "
                                                         + "Home Office reference no: {}, "
                                                         + "Home Office API response code: {}";

    private final HomeOfficeApi<AsylumCase> homeOfficeApi;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LAST; // this handler MUST run after  AppealReferenceNumberHandler
    }

    public AppealSubmittedNotifyHomeOfficeHandler(
        @Value("${featureFlag.isHomeOfficeIntegrationEnabled}") boolean isHomeOfficeIntegrationEnabled,
        HomeOfficeApi<AsylumCase> homeOfficeApi) {
        this.homeOfficeApi = homeOfficeApi;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               // This handler must run once and only once for each appeal, ideally as soon as the appeal is first created (and no longer in DRAFT state)
               && (callback.getEvent() == SUBMIT_APPEAL); // TODO: include logic to cover  callback.getEvent() == MARK_APPEAL_PAID
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        // Only proceed if the new  applications/v1/{id}  Home Office endpoint has already been called
        if (!asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class).isPresent()) {
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        // Retrieve the UAN or GWF from the case record
        final String homeOfficeReferenceNumber = HandlerUtils.getUanOrGwf(asylumCase);
        if (homeOfficeReferenceNumber.isEmpty()) {
            throw new IllegalStateException("homeOfficeReferenceNumber and gwfReferenceNumber are both missing - one or other is needed");
        }
        // Ensure this is present before calling the Home Office API (where it will be needed)
        final String appealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("Case ID for the appeal is not present"));
        // Details for logging purposes only
        final HomeOfficeApiResponseStatusType homeOfficeAppellantApiResponseStatus = asylumCase.read(
                            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, HomeOfficeApiResponseStatusType.class)
                            .orElse(HomeOfficeApiResponseStatusType.UNKNOWN);
        final long caseId = callback.getCaseDetails().getId();

        log.info("Start: Sending Home Office notification - " + SUPPRESSION_LOG_FIELDS_NEW,
            callback.getEvent(), caseId, appealReferenceNumber, homeOfficeReferenceNumber, homeOfficeAppellantApiResponseStatus.getStatusCode());

        asylumCase = homeOfficeApi.aboutToSubmit(callback);

        log.info("Finish: Sending Home Office notification - " + SUPPRESSION_LOG_FIELDS_NEW,
            callback.getEvent(), caseId, appealReferenceNumber, homeOfficeReferenceNumber, homeOfficeAppellantApiResponseStatus.getStatusCode());

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
