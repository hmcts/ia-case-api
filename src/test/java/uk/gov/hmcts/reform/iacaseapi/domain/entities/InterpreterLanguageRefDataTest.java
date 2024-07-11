package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class InterpreterLanguageRefDataTest {

    private static final String YES = "Yes";
    private static final String MANUAL_LANG_DESCRIPTION = "manualLangDescription";
    private static final String MANUAL_LANG_DESCRIPTION_2 = "manualLangDescription2";


    @Mock
    private DynamicList dynamicList;
    @Mock
    private DynamicList dynamicList2;

    private InterpreterLanguageRefData interpreterLanguageRefData;

    @Test
    void should_initialize_correctly_with_all_args() {
        interpreterLanguageRefData = new InterpreterLanguageRefData(
            dynamicList,
            List.of(YES),
            MANUAL_LANG_DESCRIPTION
        );

        assertEquals(dynamicList, interpreterLanguageRefData.getLanguageRefData());
        assertEquals(List.of(YES), interpreterLanguageRefData.getLanguageManualEntry());
        assertEquals(MANUAL_LANG_DESCRIPTION, interpreterLanguageRefData.getLanguageManualEntryDescription());
    }

    @Test
    void should_initialize_correctly_with_no_args() {
        interpreterLanguageRefData = new InterpreterLanguageRefData();

        assertNull(interpreterLanguageRefData.getLanguageRefData());
        assertNull(interpreterLanguageRefData.getLanguageManualEntry());
        assertNull(interpreterLanguageRefData.getLanguageManualEntryDescription());
    }

    @Test
    void should_implement_equals_method_correctly() {
        interpreterLanguageRefData = new InterpreterLanguageRefData(
            dynamicList,
            List.of(YES),
            MANUAL_LANG_DESCRIPTION
        );

        assertNotEquals(interpreterLanguageRefData, new InterpreterLanguageRefData(
            dynamicList2,
            List.of(YES),
            MANUAL_LANG_DESCRIPTION
        ));

        assertNotEquals(interpreterLanguageRefData, new InterpreterLanguageRefData(
            dynamicList,
            Collections.emptyList(),
            MANUAL_LANG_DESCRIPTION
        ));

        assertNotEquals(interpreterLanguageRefData, new InterpreterLanguageRefData(
            dynamicList,
            List.of(YES),
            MANUAL_LANG_DESCRIPTION_2
        ));
    }

}
