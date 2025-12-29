package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OrganisationPolicy;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class RemoveRepresentationPreparer implements PreSubmitCallbackHandler<BailCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && (callback.getEvent() ==  Event.REMOVE_BAIL_LEGAL_REPRESENTATIVE
                    || callback.getEvent() ==  Event.STOP_LEGAL_REPRESENTING);
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase = callback.getCaseDetails().getCaseData();

        PreSubmitCallbackResponse<BailCase> response = new PreSubmitCallbackResponse<>(bailCase);

        if (bailCase.read(BailCaseFieldDefinition.LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class).isEmpty()) {
            response.addError("You cannot use this feature because the legal representative does not have "
                              + "a MyHMCTS account.");
            response.addError("If you are a legal representative, you must contact all parties confirming "
                              + "you no longer represent this client.");
            return response;
        } else {
            Value caseRole = new Value("[LEGALREPRESENTATIVE]", "Legal Representative");
            bailCase.write(
                BailCaseFieldDefinition.CHANGE_ORGANISATION_REQUEST_FIELD,
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
