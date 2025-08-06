package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_PARTY_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_SPONSOR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_INDIVIDUAL_PARTY_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_ORGANISATION_PARTY_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_PARTY_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;

public class PartyIdService {

    private PartyIdService() {
    }

    public static void appendWitnessPartyId(AsylumCase asylumCase) {

        Optional<List<IdValue<WitnessDetails>>> witnessDetailsOptional = asylumCase.read(WITNESS_DETAILS);

        AtomicInteger index = new AtomicInteger(1);
        List<IdValue<WitnessDetails>> newWitnessDetails =
            witnessDetailsOptional.orElse(emptyList())
                .stream()
                .map(idValue -> new IdValue<>(
                    String.valueOf(index.getAndIncrement()),
                    new WitnessDetails(
                        defaultIfNull(idValue.getValue().getWitnessPartyId(), HearingPartyIdGenerator.generate()),
                        idValue.getValue().getWitnessName(),
                        idValue.getValue().getWitnessFamilyName(),
                        idValue.getValue().getIsWitnessDeleted()
                    )
                ))
                .collect(toList());

        asylumCase.write(WITNESS_DETAILS, newWitnessDetails);
    }

    public static void setAppellantPartyId(AsylumCase asylumCase) {

        if (asylumCase.read(APPELLANT_PARTY_ID, String.class).orElse("").isEmpty()) {
            asylumCase.write(APPELLANT_PARTY_ID, HearingPartyIdGenerator.generate());
        }
    }

    public static void setLegalRepPartyId(AsylumCase asylumCase) {

        if (!HandlerUtils.isAipJourney(asylumCase)) {
            if (asylumCase.read(LEGAL_REP_INDIVIDUAL_PARTY_ID, String.class).orElse("").isEmpty()) {
                asylumCase.write(LEGAL_REP_INDIVIDUAL_PARTY_ID, HearingPartyIdGenerator.generate());
            }

            if (asylumCase.read(LEGAL_REP_ORGANISATION_PARTY_ID, String.class).orElse("").isEmpty()) {
                asylumCase.write(LEGAL_REP_ORGANISATION_PARTY_ID, HearingPartyIdGenerator.generate());
            }
        }
    }

    public static void resetLegalRepPartyId(AsylumCase asylumCase) {
        if (!HandlerUtils.isAipJourney(asylumCase)) {
            asylumCase.write(LEGAL_REP_INDIVIDUAL_PARTY_ID, HearingPartyIdGenerator.generate());
            asylumCase.write(LEGAL_REP_ORGANISATION_PARTY_ID, HearingPartyIdGenerator.generate());
        }
    }

    public static void setSponsorPartyId(AsylumCase asylumCase) {
        boolean hasSponsor = asylumCase.read(HAS_SPONSOR, YesOrNo.class)
            .map(flag -> flag == YES)
            .orElse(false);

        if (hasSponsor) {
            if (asylumCase.read(SPONSOR_PARTY_ID, String.class).orElse("").isEmpty()) {
                asylumCase.write(SPONSOR_PARTY_ID, HearingPartyIdGenerator.generate());
            }
        }
    }
}
