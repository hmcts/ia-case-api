package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.PartyIdGenerator;

@Component
public class PartyIdHandler implements PreSubmitCallbackHandler<BailCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && Set.of(
            START_APPLICATION,
            EDIT_BAIL_APPLICATION,
            EDIT_BAIL_APPLICATION_AFTER_SUBMIT,
            MAKE_NEW_APPLICATION).contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        if (Set.of(START_APPLICATION, EDIT_BAIL_APPLICATION, EDIT_BAIL_APPLICATION_AFTER_SUBMIT, MAKE_NEW_APPLICATION).contains(callback.getEvent())) {
            setAppellantPartyId(bailCase);
            setLegalRepPartyId(bailCase);
            setSupporterPartyIds(bailCase);

            if (Set.of(EDIT_BAIL_APPLICATION, EDIT_BAIL_APPLICATION_AFTER_SUBMIT).contains(callback.getEvent())) {
                clearSupporterPartyIds(bailCase);
                clearLegalRepPartyIds(bailCase);
            }
        }
        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private void setAppellantPartyId(BailCase bailCase) {

        if (bailCase.read(APPLICANT_PARTY_ID, String.class).orElse("").isEmpty()) {
            bailCase.write(APPLICANT_PARTY_ID, PartyIdGenerator.generate());
        }
    }

    private void setLegalRepPartyId(BailCase bailCase) {

        if (bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class).orElse(NO) == YES) {
            if (bailCase.read(LEGAL_REP_INDIVIDUAL_PARTY_ID, String.class).orElse("").isEmpty()) {
                bailCase.write(LEGAL_REP_INDIVIDUAL_PARTY_ID, PartyIdGenerator.generate());
            }

            if (bailCase.read(LEGAL_REP_ORGANISATION_PARTY_ID, String.class).orElse("").isEmpty()) {
                bailCase.write(LEGAL_REP_ORGANISATION_PARTY_ID, PartyIdGenerator.generate());
            }
        }
    }

    private void setSupporterPartyIds(BailCase bailCase) {

        if (bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class).orElse(NO) == YES) {
            if (bailCase.read(SUPPORTER_1_PARTY_ID, String.class).orElse("").isEmpty()) {
                bailCase.write(SUPPORTER_1_PARTY_ID, PartyIdGenerator.generate());
            }
        }

        if (bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class).orElse(NO) == YES) {
            if (bailCase.read(SUPPORTER_2_PARTY_ID, String.class).orElse("").isEmpty()) {
                bailCase.write(SUPPORTER_2_PARTY_ID, PartyIdGenerator.generate());
            }
        }

        if (bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class).orElse(NO) == YES) {
            if (bailCase.read(SUPPORTER_3_PARTY_ID, String.class).orElse("").isEmpty()) {
                bailCase.write(SUPPORTER_3_PARTY_ID, PartyIdGenerator.generate());
            }
        }

        if (bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_4, YesOrNo.class).orElse(NO) == YES) {
            if (bailCase.read(SUPPORTER_4_PARTY_ID, String.class).orElse("").isEmpty()) {
                bailCase.write(SUPPORTER_4_PARTY_ID, PartyIdGenerator.generate());
            }
        }
    }

    private void clearSupporterPartyIds(BailCase bailCase) {

        if (bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class).orElse(NO) == NO) {
            if (!bailCase.read(SUPPORTER_1_PARTY_ID, String.class).orElse("").isEmpty()) {
                bailCase.clear(SUPPORTER_1_PARTY_ID);
                bailCase.clear(SUPPORTER_2_PARTY_ID);
                bailCase.clear(SUPPORTER_3_PARTY_ID);
                bailCase.clear(SUPPORTER_4_PARTY_ID);
            }
        }

        if (bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class).orElse(NO) == NO) {
            if (!bailCase.read(SUPPORTER_2_PARTY_ID, String.class).orElse("").isEmpty()) {
                bailCase.clear(SUPPORTER_2_PARTY_ID);
                bailCase.clear(SUPPORTER_3_PARTY_ID);
                bailCase.clear(SUPPORTER_4_PARTY_ID);
            }
        }

        if (bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class).orElse(NO) == NO) {
            if (!bailCase.read(SUPPORTER_3_PARTY_ID, String.class).orElse("").isEmpty()) {
                bailCase.clear(SUPPORTER_3_PARTY_ID);
                bailCase.clear(SUPPORTER_4_PARTY_ID);
            }
        }

        if (bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_4, YesOrNo.class).orElse(NO) == NO) {
            if (!bailCase.read(SUPPORTER_4_PARTY_ID, String.class).orElse("").isEmpty()) {
                bailCase.clear(SUPPORTER_4_PARTY_ID);
            }
        }
    }

    private void clearLegalRepPartyIds(BailCase bailCase) {

        if (bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class).orElse(NO) == NO) {
            if (!bailCase.read(LEGAL_REP_INDIVIDUAL_PARTY_ID, String.class).orElse("").isEmpty()) {
                bailCase.clear(LEGAL_REP_INDIVIDUAL_PARTY_ID);
            }

            if (!bailCase.read(LEGAL_REP_ORGANISATION_PARTY_ID, String.class).orElse("").isEmpty()) {
                bailCase.clear(LEGAL_REP_ORGANISATION_PARTY_ID);
            }
        }
    }
}
