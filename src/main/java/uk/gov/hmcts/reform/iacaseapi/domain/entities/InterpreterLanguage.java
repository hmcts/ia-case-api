package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InterpreterLanguage {

    //legacy object (superseded by InterpreterLanguageRefData)
    private String language;
    private String languageDialect;

}
