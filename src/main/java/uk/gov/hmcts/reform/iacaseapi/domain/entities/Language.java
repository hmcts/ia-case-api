package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Getter
public class Language {

    private final String languageCode;
    private final String languageText;

    public Language(String languageCode, String languageName) {
        this.languageCode = languageCode;
        this.languageText = languageName;
    }

    public Language(String languageName) {
        this(null, languageName);
    }

    public static Language of(InterpreterLanguageRefData interpreterLanguageRefData) {
        if (interpreterLanguageRefData.getLanguageRefData() != null
            && interpreterLanguageRefData.getLanguageRefData().getValue() != null) {
            String languageCode = interpreterLanguageRefData.getLanguageRefData().getValue().getCode();
            String languageName = interpreterLanguageRefData.getLanguageRefData().getValue().getLabel();
                return new Language(languageCode, languageName);
        } else if (interpreterLanguageRefData.getLanguageManualEntry() != null
            && !interpreterLanguageRefData.getLanguageManualEntry().isEmpty()) {
            return new Language(interpreterLanguageRefData.getLanguageManualEntryDescription());
        }

        return new Language(null);
    }

    public boolean isEmpty() {
        return (languageCode == null || languageCode.isEmpty())
            && (languageText == null || languageText.isEmpty());
    }
}
