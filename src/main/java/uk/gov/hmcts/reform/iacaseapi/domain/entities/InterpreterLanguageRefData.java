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
public class InterpreterLanguageRefData {

    private DynamicList interpreterLanguageRd;
    private List<String> languageManualEnter;
    private String manualLanguageDescription;

}
