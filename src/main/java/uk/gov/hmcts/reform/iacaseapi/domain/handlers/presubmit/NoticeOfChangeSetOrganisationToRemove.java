package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CallbackApiDelegator;

@Component
public class NoticeOfChangeSetOrganisationToRemove implements PreSubmitCallbackHandler<AsylumCase> {

    private final CallbackApiDelegator apiDelegator;
    private final String aacUrl;
    private final String setSetOrganisationToRemoveApiPath;

    public NoticeOfChangeSetOrganisationToRemove(
            CallbackApiDelegator apiDelegator,
            @Value("${assign_case_access_api_url}") String aacUrl,
            @Value("${noc_set_organisation_to_remove_path}") String setSetOrganisationToRemoveApiPath
    ) {
        this.apiDelegator = apiDelegator;
        this.aacUrl = aacUrl;
        this.setSetOrganisationToRemoveApiPath = setSetOrganisationToRemoveApiPath;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && (callback.getEvent() == Event.REMOVE_REPRESENTATION
                    || callback.getEvent() == Event.REMOVE_LEGAL_REPRESENTATIVE);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = apiDelegator.delegate(callback, aacUrl + setSetOrganisationToRemoveApiPath);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
