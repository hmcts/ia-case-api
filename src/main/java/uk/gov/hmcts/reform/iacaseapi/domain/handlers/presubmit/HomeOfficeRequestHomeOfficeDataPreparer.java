package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_CASE_STATUS_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_SEARCH_NO_MATCH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_SEARCH_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REQUEST_HOME_OFFICE_DATA;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Component
public class HomeOfficeRequestHomeOfficeDataPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String HOME_OFFICE_DATA_PRESENT_MESSAGE = "The Home Office data "
        + "has already been retrieved successfully "
        + "and is available in the validation tab.";
    private final boolean isHomeOfficeIntegrationEnabled;

    public HomeOfficeRequestHomeOfficeDataPreparer(
        @Value("${featureFlag.isHomeOfficeIntegrationEnabled}") boolean isHomeOfficeIntegrationEnabled) {
        this.isHomeOfficeIntegrationEnabled = isHomeOfficeIntegrationEnabled;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return isHomeOfficeIntegrationEnabled
            && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
            && callback.getEvent() == REQUEST_HOME_OFFICE_DATA;
    }


    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        //Check if home office call is made when data is already fetched
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        final String homeOfficeSearchStatus = asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class).orElse("");
        final String homeOfficeSearchNoMatch = asylumCase.read(HOME_OFFICE_SEARCH_NO_MATCH, String.class).orElse("");

        if ("SUCCESS".equalsIgnoreCase(homeOfficeSearchStatus)
                && !homeOfficeSearchNoMatch.equals("NO_MATCH")
                && asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class).isPresent()) {
            log.info("Multiple call error:" + HOME_OFFICE_DATA_PRESENT_MESSAGE);
            response.addError(HOME_OFFICE_DATA_PRESENT_MESSAGE);
        }

        return response;
    }
}
