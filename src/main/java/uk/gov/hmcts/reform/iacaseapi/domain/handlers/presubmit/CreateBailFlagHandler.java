package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag.ROLE_ON_CASE_APPLICANT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag.ROLE_ON_CASE_FCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Component
class CreateBailFlagHandler implements PreSubmitCallbackHandler<BailCase> {

    public static final List<BailCaseFieldDefinition> FCS_N_GIVEN_NAME_FIELD = List.of(
        SUPPORTER_GIVEN_NAMES,
        SUPPORTER_2_GIVEN_NAMES,
        SUPPORTER_3_GIVEN_NAMES,
        SUPPORTER_4_GIVEN_NAMES
    );

    public static final List<BailCaseFieldDefinition> FCS_N_FAMILY_NAME_FIELD = List.of(
        SUPPORTER_FAMILY_NAMES,
        SUPPORTER_2_FAMILY_NAMES,
        SUPPORTER_3_FAMILY_NAMES,
        SUPPORTER_4_FAMILY_NAMES
    );

    public static final List<BailCaseFieldDefinition> HAS_FINANCIAL_CONDITION_SUPPORTER_N = List.of(
        HAS_FINANCIAL_COND_SUPPORTER,
        HAS_FINANCIAL_COND_SUPPORTER_2,
        HAS_FINANCIAL_COND_SUPPORTER_3,
        HAS_FINANCIAL_COND_SUPPORTER_4
    );

    public static final List<BailCaseFieldDefinition> FCS_N_PARTY_ID_FIELD = List.of(
        SUPPORTER_1_PARTY_ID,
        SUPPORTER_2_PARTY_ID,
        SUPPORTER_3_PARTY_ID,
        SUPPORTER_4_PARTY_ID
    );

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.CREATE_FLAG;
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

        Optional<StrategicCaseFlag> existingCaseLevelFlags = bailCase.read(CASE_FLAGS);

        Optional<StrategicCaseFlag> existingAppellantLevelFlags = bailCase.read(APPELLANT_LEVEL_FLAGS);

        if (existingAppellantLevelFlags.isEmpty()
            || existingAppellantLevelFlags.get().getPartyName() == null
            || existingAppellantLevelFlags.get().getPartyName().isBlank()) {

            final String appellantNameForDisplay =
                bailCase
                    .read(APPLICANT_FULL_NAME, String.class)
                    .orElseThrow(() -> new IllegalStateException("applicantFullName is not present"));

            bailCase.write(APPELLANT_LEVEL_FLAGS, new StrategicCaseFlag(appellantNameForDisplay, ROLE_ON_CASE_APPLICANT));
        } else {
            log.info("Existing Appellant Level flags: {}", existingAppellantLevelFlags);
        }

        handleFcsLevelFlags(bailCase);

        if (existingCaseLevelFlags.isEmpty()) {
            bailCase.write(CASE_FLAGS, new StrategicCaseFlag());
        }  else {
            log.info("Existing Case Level flags: {}", existingCaseLevelFlags);
        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private void handleFcsLevelFlags(BailCase bailCase) {
        Optional<List<PartyFlagIdValue>> maybeFcsFlagsOptional = bailCase.read(FCS_LEVEL_FLAGS);
        List<PartyFlagIdValue> fcsFlags = maybeFcsFlagsOptional.orElse(Collections.emptyList());

        List<PartyFlagIdValue> newFcsLevelFlags = new ArrayList<>();

        int i = 0;
        while (i < 4) {
            boolean hasFcs = bailCase.read(HAS_FINANCIAL_CONDITION_SUPPORTER_N.get(i), YesOrNo.class)
                .map(yesOrNo -> YES == yesOrNo).orElse(false);
            if (hasFcs) {
                int finalI = i;
                String partyId = bailCase.read(FCS_N_PARTY_ID_FIELD.get(i), String.class).orElse(null);

                if (partyId == null) {
                    // Flags are created only for financial condition supporter with party ID set to a non-null value
                    i++;
                    continue;
                }

                fcsFlags.stream()
                    .filter(f -> f.getPartyId().equals(partyId))
                    .findFirst()
                    .ifPresentOrElse(
                        existingFcsFlags -> newFcsLevelFlags.add(new PartyFlagIdValue(
                            partyId, new StrategicCaseFlag(buildFcsFullName(bailCase, finalI), ROLE_ON_CASE_FCS,
                                                           existingFcsFlags.getValue().getDetails()))),
                        () -> newFcsLevelFlags.add(new PartyFlagIdValue(
                            partyId, new StrategicCaseFlag(buildFcsFullName(bailCase, finalI), ROLE_ON_CASE_FCS)))
                    );
            }
            i++;
        }

        bailCase.write(FCS_LEVEL_FLAGS, newFcsLevelFlags);

    }

    private String buildFcsFullName(BailCase bailCase, int index) {
        String givenNames =  bailCase.read(FCS_N_GIVEN_NAME_FIELD.get(index), String.class)
            .orElseThrow(() -> new IllegalStateException(FCS_N_GIVEN_NAME_FIELD.get(index).value() + " is not present"));
        String familyName = bailCase.read(FCS_N_FAMILY_NAME_FIELD.get(index), String.class)
            .orElseThrow(() -> new IllegalStateException(FCS_N_FAMILY_NAME_FIELD.get(index).value() + " is not present"));

        return givenNames + " " + familyName;
    }
}
