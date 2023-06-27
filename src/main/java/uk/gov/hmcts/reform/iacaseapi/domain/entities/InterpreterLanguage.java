package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InterpreterLanguage {

    private String language; // legacy field
    private DynamicList interpreterLanguageRd;
    private String languageDialect; // legacy field
    private List<String> languageManualEnter;
    private String manualLanguageDescription;

}
