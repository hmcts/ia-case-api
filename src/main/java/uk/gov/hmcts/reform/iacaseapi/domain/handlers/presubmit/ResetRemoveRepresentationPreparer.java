package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Component
public class ResetRemoveRepresentationPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.RESET_REMOVE_REPRESENTATION;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        if (isValidChangeOrganisationRequest(asylumCase)) {

            response.addError("No changes required to reset Change representation event");
        }

        return response;
    }

    private boolean isValidChangeOrganisationRequest(AsylumCase asylumCase) {

        Optional<ChangeOrganisationRequest> changeOrganisationRequestOptional = asylumCase.read(
                AsylumCaseFieldDefinition.CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class);

        if (changeOrganisationRequestOptional.isEmpty()) {
            return true;
        }

        ChangeOrganisationRequest request = changeOrganisationRequestOptional.get();

        return (request.getCaseRoleId() == null || isEmpty(request.getCaseRoleId().getListItems()))
                && StringUtils.isEmpty(request.getRequestTimestamp())
                && StringUtils.isEmpty(request.getApprovalStatus());
    }
}

