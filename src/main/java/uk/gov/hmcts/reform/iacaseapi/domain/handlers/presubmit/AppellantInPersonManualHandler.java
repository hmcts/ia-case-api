package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.APPELLANT_IN_PERSON_MANUAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

@Slf4j
@Component
public class AppellantInPersonManualHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == APPELLANT_IN_PERSON_MANUAL;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        boolean isAdmin = HandlerUtils.isAdmin(asylumCase);
        boolean hasAddedLegalRepDetails = HandlerUtils.hasAddedLegalRepDetails(asylumCase);
        boolean isRepJourney = HandlerUtils.isRepJourney(asylumCase);

        if (isAdmin || (hasAddedLegalRepDetails && isRepJourney)) {
            PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
            response.addError("You cannot request Appellant in Person - Manual for this appeal");

            return response;
        }

        if (!hasAddedLegalRepDetails && isRepJourney) {
            asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REP_NAME);
            asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_NAME);
            asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS);
            asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REP_COMPANY);
            asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REP_COMPANY_NAME);
            asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REP_COMPANY_ADDRESS);
            asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REP_REFERENCE_NUMBER);
            asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REP_INDIVIDUAL_PARTY_ID);
            asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REP_ORGANISATION_PARTY_ID);
        }

        asylumCase.write(APPELLANTS_REPRESENTATION, YES);
        asylumCase.write(IS_ADMIN, YES);
        asylumCase.clear(JOURNEY_TYPE);

        // remove field which is used to suppress notifications after appeal is transferred to another Legal Rep firm
        asylumCase.clear(CHANGE_ORGANISATION_REQUEST_FIELD);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}