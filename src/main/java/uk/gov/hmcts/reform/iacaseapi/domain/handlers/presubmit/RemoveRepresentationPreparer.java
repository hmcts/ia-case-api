package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.OrganisationPolicy;

@Component
public class RemoveRepresentationPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && (callback.getEvent() == Event.REMOVE_REPRESENTATION
                   || callback.getEvent() == Event.REMOVE_LEGAL_REPRESENTATIVE);
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

        Optional<OrganisationPolicy> localAuthorityPolicy = callback.getCaseDetails().getCaseData().read(AsylumCaseFieldDefinition.LOCAL_AUTHORITY_POLICY);
        if (localAuthorityPolicy.isEmpty()) {
            // For appeals submitted before 10 February 2021, localAuthorityPolicy was not added to the case data
            response.addError("You cannot use this feature because the legal representative does not have a MyHMCTS account or the appeal was created before 10 February 2021.");
            response.addError("If you are a legal representative, you must contact all parties confirming you no longer represent this client.");

        } else if (localAuthorityPolicy.get().getOrganisation() == null
                || isEmpty(localAuthorityPolicy.get().getOrganisation().getOrganisationID())) {
            response.addError("This appellant is not currently represented so Notice of Change cannot be actioned. Please contact the Service Desk giving this error message.");
        } else {

            Value caseRole = new Value("[LEGALREPRESENTATIVE]", "Legal Representative");
            asylumCase.write(
                AsylumCaseFieldDefinition.CHANGE_ORGANISATION_REQUEST_FIELD,
                new ChangeOrganisationRequest(
                    new DynamicList(caseRole, newArrayList(caseRole)),
                    LocalDateTime.now().toString(),
                    "1"
                )
            );

        }
        return response;
    }
}


