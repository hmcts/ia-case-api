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

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

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
                String legalRepIndividualPartyId = asylumCase.read(LEGAL_REP_INDIVIDUAL_PARTY_ID, String.class).orElse("");
                String legalRepOrganisationPartyId = asylumCase.read(LEGAL_REP_ORGANISATION_PARTY_ID, String.class).orElse("");

                boolean isAppealOutOfCountry = asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)
                        .map(flag -> flag == YES)
                        .orElse(false);
                boolean isHasSponse = asylumCase.read(HAS_SPONSOR, YesOrNo.class)
                        .map(flag -> flag == YES)
                        .orElse(false);
                ;

                if (appellantPartyId.isEmpty()) {
                    asylumCase.write(APPELLANT_PARTY_ID, getPartyId());
                }

                if (legalRepIndividualPartyId.isEmpty()) {
                    asylumCase.write(LEGAL_REP_INDIVIDUAL_PARTY_ID, getPartyId());
                }

                if (legalRepOrganisationPartyId.isEmpty()) {
                    asylumCase.write(LEGAL_REP_ORGANISATION_PARTY_ID, getPartyId());
                }

                if (isAppealOutOfCountry && isHasSponse) {
                    String sponsorPartyId = asylumCase.read(SPONSOR_PARTY_ID, String.class).orElse("");
                    if (sponsorPartyId.isEmpty()) {
                        asylumCase.write(SPONSOR_PARTY_ID, getPartyId());
                    }
                }
                break;

            case DRAFT_HEARING_REQUIREMENTS:
                Optional<List<IdValue<WitnessDetails>>> witnessDetails = asylumCase.read(WITNESS_DETAILS);

                if (witnessDetails.isPresent() && witnessDetails.get().size() > 0) {

                    for (int i = 0; i < witnessDetails.get().size(); i++) {
                        witnessDetails.get().get(i).getValue().setWitnessPartyId(getPartyId());
                    }
                    asylumCase.write(WITNESS_DETAILS, witnessDetails);
                }
                break;
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private String getPartyId() {
        return UUID.randomUUID().toString();
    }

}
