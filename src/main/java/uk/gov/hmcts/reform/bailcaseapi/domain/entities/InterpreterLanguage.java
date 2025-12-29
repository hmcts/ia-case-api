package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InterpreterLanguage {
    private String language;
    private String languageDialect;
}
