package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_COMPANY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_PHONE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_REFERENCE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPDATE_LEGAL_REP_COMPANY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPDATE_LEGAL_REP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPDATE_LEGAL_REP_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPDATE_LEGAL_REP_PHONE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPDATE_LEGAL_REP_REFERENCE;

@Slf4j
@Component
public class LegalRepresentativeUpdateDetailsHandler implements PreSubmitCallbackHandler<BailCase> {

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
        String email = bailCase.read(UPDATE_LEGAL_REP_EMAIL_ADDRESS, String.class).orElse("");
        String phoneNumber = bailCase.read(UPDATE_LEGAL_REP_PHONE, String.class).orElse("");
        String reference = bailCase.read(UPDATE_LEGAL_REP_REFERENCE, String.class).orElse("");

        bailCase.clear(UPDATE_LEGAL_REP_COMPANY);
        bailCase.clear(UPDATE_LEGAL_REP_NAME);
        bailCase.clear(UPDATE_LEGAL_REP_EMAIL_ADDRESS);
        bailCase.clear(UPDATE_LEGAL_REP_PHONE);
        bailCase.clear(UPDATE_LEGAL_REP_REFERENCE);

        bailCase.write(LEGAL_REP_COMPANY, company);
        bailCase.write(LEGAL_REP_NAME, name);
        bailCase.write(LEGAL_REP_EMAIL_ADDRESS, email);
        bailCase.write(LEGAL_REP_PHONE, phoneNumber);
        bailCase.write(LEGAL_REP_REFERENCE, reference);

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
