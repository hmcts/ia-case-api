package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_PARTY_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_SPONSOR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_INDIVIDUAL_PARTY_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_ORGANISATION_PARTY_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_PARTY_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingPartyIdGenerator;

@Component
public class PartyIdHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && Set.of(
                START_APPEAL,
                EDIT_APPEAL,
                Event.EDIT_APPEAL_AFTER_SUBMIT).contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        if (Set.of(START_APPEAL, EDIT_APPEAL).contains(callback.getEvent())) {
            setAppellantPartyId(asylumCase);
            setLegalRepPartyId(asylumCase);
        }

        setSponsorPartyId(asylumCase);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void setAppellantPartyId(AsylumCase asylumCase) {

        if (asylumCase.read(APPELLANT_PARTY_ID, String.class).orElse("").isEmpty()) {
            asylumCase.write(APPELLANT_PARTY_ID, HearingPartyIdGenerator.generate());
        }
    }

    private void setLegalRepPartyId(AsylumCase asylumCase) {

        if (asylumCase.read(LEGAL_REP_INDIVIDUAL_PARTY_ID, String.class).orElse("").isEmpty()) {
            asylumCase.write(LEGAL_REP_INDIVIDUAL_PARTY_ID, HearingPartyIdGenerator.generate());
        }

        if (asylumCase.read(LEGAL_REP_ORGANISATION_PARTY_ID, String.class).orElse("").isEmpty()) {
            asylumCase.write(LEGAL_REP_ORGANISATION_PARTY_ID, HearingPartyIdGenerator.generate());
        }
    }

    private void setSponsorPartyId(AsylumCase asylumCase) {

        boolean isAppealOutOfCountry = asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)
                .map(flag -> flag == NO)
                .orElse(false);
        boolean hasSponsor = asylumCase.read(HAS_SPONSOR, YesOrNo.class)
                .map(flag -> flag == YES)
                .orElse(false);

        if (isAppealOutOfCountry && hasSponsor) {
            if (asylumCase.read(SPONSOR_PARTY_ID, String.class).orElse("").isEmpty()) {
                asylumCase.write(SPONSOR_PARTY_ID, HearingPartyIdGenerator.generate());
            }
        }
    }

}
