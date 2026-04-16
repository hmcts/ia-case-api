package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StrategicCaseFlagType {

    ANONYMITY("CF0012", "RRO (Restricted Reporting Order / Anonymisation)"),
    HEARING_LOOP("RA0043", "Hearing loop (hearing enhancement system)"),
    STEP_FREE_WHEELCHAIR_ACCESS("RA0019", "Step free / wheelchair access"),
    CASE_GIVEN_IN_PRIVATE("SM0004", "Evidence given in private"),
    SIGN_LANGUAGE_INTERPRETER("RA0042", "Sign Language Interpreter"),
    INTERPRETER_LANGUAGE_FLAG("PF0015", "Language Interpreter"),
    AUDIO_VIDEO_EVIDENCE("PF0014", "Audio / Video Evidence"),
    LANGUAGE_INTERPRETER("PF0015", "Language Interpreter"),
    LITIGATION_FRIEND("PF0017", "Litigation friend"),
    LACKING_CAPACITY("PF0018", "Lacking capacity"),
    PRESIDENTIAL_PANEL("CF0011", "Presidential panel"),
    FOREIGN_NATIONAL_OFFENDER("PF0012", "Foreign national offender"),
    DETAINED_INDIVIDUAL("PF0019", "Detained individual");

    @JsonValue
    private final String flagCode;
    private final String name;

    StrategicCaseFlagType(String flagCode, String name) {
        this.flagCode = flagCode;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getFlagCode() {
        return flagCode;
    }

    @Override
    public String toString() {
        return getFlagCode();
    }
}
