package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

public final class FlagHandler {

    private FlagHandler() {
        // Utility class - no instantiation
    }

    /**
     * Handles activation of a flag for an asylum case.
     * 
     * @param asylumCase The asylum case to update
     * @param flagField The field definition where the flag should be stored
     * @param flagType The type of flag to activate
     * @param dateProvider Date provider for timestamp
     * @return true if a new flag was created, false if the flag already existed
     */
    public static boolean activateFlag(
            AsylumCase asylumCase, 
            AsylumCaseFieldDefinition flagField,
            StrategicCaseFlagType flagType, 
            DateProvider dateProvider) {
        
        Optional<StrategicCaseFlag> strategicCaseFlagOptional = asylumCase.read(flagField, StrategicCaseFlag.class);
        
        StrategicCaseFlagService caseFlagService = strategicCaseFlagOptional
                .map(StrategicCaseFlagService::new)
                .orElseGet(StrategicCaseFlagService::new);

        boolean newFlagNeededCreating = caseFlagService.activateFlag(
                flagType, YesOrNo.YES, dateProvider.nowWithTime().toString());

        if (newFlagNeededCreating) {
            asylumCase.write(flagField, caseFlagService.getStrategicCaseFlag());
        }
        
        return newFlagNeededCreating;
    }
} 