package uk.gov.hmcts.reform.iacaseapi.domain.entities;

public enum InterpreterLanguageCategory {

    SPOKEN_LANGUAGE_INTERPRETER("spokenLanguageInterpreter"),
    SIGN_LANGUAGE_INTERPRETER("signLanguageInterpreter");

    private final String value;

    InterpreterLanguageCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
