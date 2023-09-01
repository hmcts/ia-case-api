package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.InterpreterLanguagesUtils.WITNESS_N_FIELD;

@Slf4j
@Component
public class PartyIdHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && Arrays.asList(
                Event.START_APPEAL,
                Event.EDIT_APPEAL,
                Event.DRAFT_HEARING_REQUIREMENTS).contains(callback.getEvent());
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

        switch (callback.getEvent()) {

            case START_APPEAL:
            case EDIT_APPEAL:

                String appellantPartyId = asylumCase.read(APPELLANT_PARTY_ID, String.class).orElse("");
                if (appellantPartyId.isEmpty()) {
                    asylumCase.write(APPELLANT_PARTY_ID, generatePartyId());
                }

                String legalRepIndividualPartyId = asylumCase.read(LEGAL_REP_INDIVIDUAL_PARTY_ID, String.class).orElse("");
                if (legalRepIndividualPartyId.isEmpty()) {
                    asylumCase.write(LEGAL_REP_INDIVIDUAL_PARTY_ID, generatePartyId());
                }

                String legalRepOrganisationPartyId = asylumCase.read(LEGAL_REP_ORGANISATION_PARTY_ID, String.class).orElse("");
                if (legalRepOrganisationPartyId.isEmpty()) {
                    asylumCase.write(LEGAL_REP_ORGANISATION_PARTY_ID, generatePartyId());
                }

                AtomicReference<YesOrNo> outOfCountry = new AtomicReference<>(NO);
                asylumCase.read(APPELLANT_IN_UK, YesOrNo.class).ifPresent(
                        appellantInUk -> outOfCountry.set(appellantInUk.equals(NO) ? YES : NO)
                );
                boolean isHasSponsor = asylumCase.read(HAS_SPONSOR, YesOrNo.class)
                        .map(flag -> flag == YES)
                        .orElse(false);
                if (outOfCountry.get().equals(YES) && isHasSponsor) {
                    String sponsorPartyId = asylumCase.read(SPONSOR_PARTY_ID, String.class).orElse("");
                    if (sponsorPartyId.isEmpty()) {
                        asylumCase.write(SPONSOR_PARTY_ID, generatePartyId());
                    }
                }
                break;

            case DRAFT_HEARING_REQUIREMENTS:
                Optional<List<IdValue<WitnessDetails>>> witnessDetails = asylumCase.read(WITNESS_DETAILS);

                if (witnessDetails.isPresent() && witnessDetails.get().size() > 0) {

                    for (int i = 0; i < witnessDetails.get().size(); i++) {
                        String witnessPartyId = generatePartyId();

                        if (witnessDetails.get().get(i).getValue().getWitnessPartyId() == null) {
                            witnessDetails.get().get(i).getValue().setWitnessPartyId(witnessPartyId);
                        }

                        WitnessDetails witness = asylumCase.read(WITNESS_N_FIELD.get(i), WitnessDetails.class).orElse(null);
                        if (witness != null && witness.getWitnessPartyId() == null) {
                            witness.setWitnessPartyId(witnessPartyId);
                            asylumCase.write(WITNESS_N_FIELD.get(i), witness);
                        }
                    }
                    asylumCase.write(WITNESS_DETAILS, witnessDetails);
                }
                break;

            default:
                break;
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private String generatePartyId() {
        return UUID.randomUUID().toString();
    }

}
