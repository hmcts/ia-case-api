package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;

public class InterpreterRequirement {

    private Optional<String> language = Optional.empty();
    private Optional<String> dialect = Optional.empty();

    private InterpreterRequirement() {
        // noop -- for deserializer
    }

    public InterpreterRequirement(
        String language,
        String dialect
    ) {
        this.language = Optional.ofNullable(language);
        this.dialect = Optional.ofNullable(dialect);
    }

    public Optional<String> getLanguage() {
        return language;
    }

    public Optional<String> getDialect() {
        return dialect;
    }

    public void setLanguage(String language) {
        this.language = Optional.ofNullable(language);
    }

    public void setDialect(String dialect) {
        this.dialect = Optional.ofNullable(dialect);
    }
}
