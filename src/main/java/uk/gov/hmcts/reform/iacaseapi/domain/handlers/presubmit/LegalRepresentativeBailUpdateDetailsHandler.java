package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Component
public class LegalRepresentativeBailUpdateDetailsHandler implements PreSubmitCallbackHandler<BailCase> {

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.UPDATE_BAIL_LEGAL_REP_DETAILS;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        String company = bailCase.read(UPDATE_LEGAL_REP_COMPANY, String.class).orElse("");
        String name = bailCase.read(UPDATE_LEGAL_REP_NAME, String.class).orElse("");
        String familyName = bailCase.read(UPDATE_LEGAL_REP_FAMILY_NAME, String.class).orElse("");
        String email = bailCase.read(UPDATE_LEGAL_REP_EMAIL_ADDRESS, String.class).orElse("");
        String phoneNumber = bailCase.read(UPDATE_LEGAL_REP_PHONE, String.class).orElse("");
        String reference = bailCase.read(UPDATE_LEGAL_REP_REFERENCE, String.class).orElse("");

        bailCase.clear(UPDATE_LEGAL_REP_COMPANY);
        bailCase.clear(UPDATE_LEGAL_REP_NAME);
        bailCase.clear(UPDATE_LEGAL_REP_FAMILY_NAME);
        bailCase.clear(UPDATE_LEGAL_REP_EMAIL_ADDRESS);
        bailCase.clear(UPDATE_LEGAL_REP_PHONE);
        bailCase.clear(UPDATE_LEGAL_REP_REFERENCE);

        bailCase.write(LEGAL_REP_COMPANY, company);
        bailCase.write(LEGAL_REP_NAME, name);
        bailCase.write(LEGAL_REP_FAMILY_NAME, familyName);
        bailCase.write(LEGAL_REP_EMAIL_ADDRESS, email);
        bailCase.write(LEGAL_REP_PHONE, phoneNumber);
        bailCase.write(LEGAL_REP_REFERENCE, reference);
        if (bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.NO) {
            bailCase.write(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.YES);
        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
