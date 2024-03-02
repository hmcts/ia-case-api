package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

public class LanguageTest {

    @Test
    public void should_hold_onto_values() {
        Language language = new Language(
            "languageCode",
            "languageText"
        );
        assertNotNull(language.getLanguageCode());
        assertNotNull(language.getLanguageText());

        language = new Language("languageText");

        assertNull(language.getLanguageCode());
        assertNotNull(language.getLanguageText());
    }

    @Test
    public void should_construct_language_from_Interpreter_language_ref_data() {
        InterpreterLanguageRefData emptyRefData = new InterpreterLanguageRefData();
        InterpreterLanguageRefData refDataDynamicValue = new InterpreterLanguageRefData(
            new DynamicList("language"), null, null);
        InterpreterLanguageRefData refDataWithManualEntry = new InterpreterLanguageRefData(
            null, List.of("language"), "language");

        assertTrue(Language.of(emptyRefData).isEmpty());
        assertFalse(Language.of(refDataDynamicValue).isEmpty());
        assertFalse(Language.of(refDataWithManualEntry).isEmpty());
    }

    @Test
    public void isEmpty_should_return_true() {
        Language language = new Language(null);

        assertTrue(language.isEmpty());
    }

    @Test
    public void isEmpty_should_return_false() {
        Language language = new Language("languageText");

        assertFalse(language.isEmpty());
    }

}