package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;


@Component
public class InternalCaseCreationContactDetailsAppender implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && Arrays.asList(Event.START_APPEAL, Event.EDIT_APPEAL).contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        YesOrNo isAdmin = asylumCase.read(IS_ADMIN, YesOrNo.class).orElse(YesOrNo.NO);


        if (isAdmin.equals(YesOrNo.YES)) {

            Optional<String> internalAppellantMobileNumber = asylumCase.read(INTERNAL_APPELLANT_MOBILE_NUMBER, String.class);
            Optional<String> internalAppellantEmail = asylumCase.read(INTERNAL_APPELLANT_EMAIL, String.class);

            if (internalAppellantMobileNumber.isPresent()) {
                asylumCase.write(MOBILE_NUMBER, internalAppellantMobileNumber);
            }
            if (internalAppellantEmail.isPresent()) {
                asylumCase.write(EMAIL, internalAppellantEmail);
            }

            if (HandlerUtils.isAppellantsRepresentation(asylumCase)) {
                asylumCase.write(HAS_ADDED_LEGAL_REP_DETAILS, NO);
                HandlerUtils.clearLegalRepFields(asylumCase);
            }
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
