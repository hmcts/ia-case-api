package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InterpreterLanguageRefData {

    private DynamicList languageRefData;
    private List<String> languageManualEntry;
    private String languageManualEntryDescription;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InterpreterLanguageRefData refData)) {
            return false;
        }
        if (!Objects.equals(languageRefData, refData.languageRefData)) {
            return false;
        }
        if (!Objects.equals(languageManualEntry, refData.languageManualEntry)) {
            return false;
        }
        return Objects.equals(languageManualEntryDescription, refData.languageManualEntryDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(languageRefData, languageManualEntry, languageManualEntryDescription);
    }
}
