package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.flagcase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMOVE_FLAG_TYPE_OF_FLAG;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
class RemoveFlagPreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    List<IdValue<CaseFlag>> expectedList;
    RemoveFlagPreparer removeFlagPreparer;
    final CaseFlag expectedCaseFlag =
        new CaseFlag(CaseFlagType.COMPLEX_CASE, "some complex flag additional info");

    @BeforeEach
    void setUp() {

        removeFlagPreparer = spy(RemoveFlagPreparer.class);
        expectedList = Collections.singletonList(new IdValue<>("1", expectedCaseFlag));
    }

    @Test
    void should_set_available_flags_to_remove() {

        when(callback.getEvent()).thenReturn(Event.REMOVE_FLAG);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(CASE_FLAGS)).thenReturn(Optional.of(expectedList));

        final List<Value> expectedElements = Optional.of(expectedList)
            .orElse(Collections.emptyList())
            .stream()
            .map(idValue -> new Value(idValue.getId(), idValue.getValue().getCaseFlagType().getReadableText()))
            .collect(Collectors.toList());

        final DynamicList dynamicList = new DynamicList(expectedElements.get(0), expectedElements);

        doReturn(expectedElements).when(removeFlagPreparer).getExistingCaseFlagListElements(expectedList);
        doReturn(dynamicList).when(removeFlagPreparer).createDynamicList(expectedElements);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            removeFlagPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(REMOVE_FLAG_TYPE_OF_FLAG, dynamicList);
    }

    @Test
    void should_throw_error_when_no_case_flags_present() {

        when(callback.getEvent()).thenReturn(Event.REMOVE_FLAG);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(CASE_FLAGS)).thenReturn(Optional.of(expectedList));

        final List<Value> expectedElements = Optional.of(expectedList)
            .orElse(Collections.emptyList())
            .stream()
            .map(idValue -> new Value(idValue.getId(), idValue.getValue().getCaseFlagType().getReadableText()))
            .collect(Collectors.toList());

        final DynamicList dynamicList = new DynamicList(expectedElements.get(0), expectedElements);

        when(asylumCase.read(CASE_FLAGS)).thenReturn(Optional.of(Collections.emptyList()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            removeFlagPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(REMOVE_FLAG_TYPE_OF_FLAG, dynamicList);
    }

    @Test
    void should_get_existing_case_flag_elements_as_list() {

        final List<Value> actualList = removeFlagPreparer.getExistingCaseFlagListElements(expectedList);
        assertEquals(1, actualList.size());
        assertEquals(expectedCaseFlag.getCaseFlagType().getReadableText(), actualList.get(0).getLabel());
    }

    @Test
    void creates_dynamic_list_from_element_list() {
        final List<Value> expectedElements = Optional.of(expectedList)
            .orElse(Collections.emptyList())
            .stream()
            .map(idValue -> new Value(idValue.getId(), idValue.getValue().getCaseFlagType().getReadableText()))
            .collect(Collectors.toList());

        final DynamicList actualDynamicList = removeFlagPreparer.createDynamicList(expectedElements);

        assertEquals(1, actualDynamicList.getListItems().size());
        assertEquals(expectedCaseFlag.getCaseFlagType().getReadableText(), actualDynamicList.getValue().getLabel());
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> removeFlagPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> removeFlagPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> removeFlagPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> removeFlagPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> removeFlagPreparer.createDynamicList(null))
            .hasMessage("elementsList must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> removeFlagPreparer.getExistingCaseFlagListElements(null))
            .hasMessage("existingCaseFlags must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = removeFlagPreparer.canHandle(callbackStage, callback);

                if (event == Event.REMOVE_FLAG
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }
}
