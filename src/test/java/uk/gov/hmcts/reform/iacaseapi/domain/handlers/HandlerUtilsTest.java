package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SOURCE_OF_APPEAL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SourceOfAppeal;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class HandlerUtilsTest {

    @Mock
    private AsylumCase asylumCase;

    @Test
    void given_journey_type_aip_returns_true() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertTrue(HandlerUtils.isAipJourney(asylumCase));
    }

    @Test
    void given_journey_type_rep_returns_true() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        assertTrue(HandlerUtils.isRepJourney(asylumCase));
    }

    @Test
    void no_journey_type_defaults_to_rep() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());
        assertTrue(HandlerUtils.isRepJourney(asylumCase));
    }

    @Test
    void given_aip_journey_rep_test_should_fail() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertFalse(HandlerUtils.isRepJourney(asylumCase));
    }

    @Test
    void given_rep_journey_aip_test_should_fail() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        assertFalse(HandlerUtils.isAipJourney(asylumCase));
    }

    @Test
    public void read_json_file_list_valid_returns_list() throws IOException {
        String filePath = "/readJsonList.json";
        List<String> expectedCaseIdList = List.of("1234", "5678", "9012");
        List<String> result = HandlerUtils.readJsonFileList(filePath, "key");
        assertEquals(expectedCaseIdList, result);
    }

    @Test
    public void read_json_file_list_invalid_file_path_throws_io() {
        String filePath = "/missingCaseIdList.json";
        assertThrows(IOException.class, () -> {
            HandlerUtils.readJsonFileList(filePath, "key");
        });
    }

    @Test
    public void read_json_file_list_empty_list_returns_empty() throws IOException {
        String filePath = "/readJsonEmptyList.json";
        List<String> result = HandlerUtils.readJsonFileList(filePath, "key");
        assertEquals(new ArrayList<>(), result);
    }

    @Test
    public void read_json_file_list_non_array_json_returns_empty() throws IOException {
        String filePath = "/readJsonNonArray.json";
        List<String> result = HandlerUtils.readJsonFileList(filePath, "key");
        assertEquals(new ArrayList<>(), result);
    }

    @Test
    void sourceOfAppeal_should_return_true() {
        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class)).thenReturn(Optional.of(SourceOfAppeal.TRANSFERRED_FROM_UPPER_TRIBUNAL));
        assertTrue(HandlerUtils.sourceOfAppeal(asylumCase));
    }

    @Test
    void sourceOfAppeal_should_return_false() {
        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class)).thenReturn(Optional.of(SourceOfAppeal.PAPER_FORM));
        assertFalse(HandlerUtils.sourceOfAppeal(asylumCase));
    }
}