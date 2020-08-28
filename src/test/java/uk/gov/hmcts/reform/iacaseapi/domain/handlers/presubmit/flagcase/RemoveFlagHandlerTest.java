package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.flagcase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagType.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
class RemoveFlagHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    final CaseFlag expectedCaseFlag =
        new CaseFlag(CaseFlagType.COMPLEX_CASE, "some complex flag additional info");
    final CaseFlag expectedCaseFlag2 =
        new CaseFlag(CaseFlagType.ANONYMITY, "some anonymity flag additional info");

    List<IdValue<CaseFlag>> expectedList;
    IdValue<CaseFlag> expectedIdValue;
    RemoveFlagHandler removeFlagHandler;

    @BeforeEach
    void setUp() {

        removeFlagHandler = new RemoveFlagHandler();
        expectedIdValue = new IdValue<>("2", expectedCaseFlag2);
        expectedList = Arrays.asList(
            new IdValue<>("1", expectedCaseFlag),
            expectedIdValue
        );
    }

    @Test
    void removes_a_specified_flag() {
        final List<Value> expectedElements = Optional.of(expectedList)
            .orElse(Collections.emptyList())
            .stream()
            .map(idValue -> new Value(idValue.getId(), idValue.getValue().getCaseFlagType().getReadableText()))
            .collect(Collectors.toList());

        final DynamicList dynamicList = new DynamicList(expectedElements.get(0), expectedElements);
        final List<IdValue<CaseFlag>> expectedCaseFlagList = new ArrayList<>();
        expectedCaseFlagList.add(expectedIdValue);

        when(callback.getEvent()).thenReturn(Event.REMOVE_FLAG);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(REMOVE_FLAG_TYPE_OF_FLAG, DynamicList.class)).thenReturn(Optional.of(dynamicList));
        when(asylumCase.read(CASE_FLAGS)).thenReturn(Optional.of(expectedList));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            removeFlagHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(REMOVE_FLAG_TYPE_OF_FLAG, DynamicList.class);
        verify(asylumCase, times(1)).read(CASE_FLAGS);

        verify(asylumCase, times(1)).write(CASE_FLAGS, expectedCaseFlagList);
        verify(asylumCase, times(1)).clear(REMOVE_FLAG_TYPE_OF_FLAG);
    }

    @ParameterizedTest
    @MethodSource("clearDisplayFlagsTestData")
    void clear_display_flags_test(CaseFlagType flagType,
                                         AsylumCaseFieldDefinition caseFlagExists,
                                         AsylumCaseFieldDefinition caseFlagAdditionalInformation) {
        removeFlagHandler.clearDisplayFlags(flagType, asylumCase);
        verify(asylumCase, times(1)).clear(caseFlagExists);
        verify(asylumCase, times(1)).clear(caseFlagAdditionalInformation);

    }

    private static Stream<Arguments> clearDisplayFlagsTestData() {

        return Stream.of(
            Arguments.of(ANONYMITY, CASE_FLAG_ANONYMITY_EXISTS, CASE_FLAG_ANONYMITY_ADDITIONAL_INFORMATION),
            Arguments.of(COMPLEX_CASE, CASE_FLAG_COMPLEX_CASE_EXISTS, CASE_FLAG_COMPLEX_CASE_ADDITIONAL_INFORMATION),
            Arguments.of(DEPORT, CASE_FLAG_DEPORT_EXISTS, CASE_FLAG_DEPORT_ADDITIONAL_INFORMATION),
            Arguments.of(DETAINED_IMMIGRATION_APPEAL, CASE_FLAG_DETAINED_IMMIGRATION_APPEAL_EXISTS, CASE_FLAG_DETAINED_IMMIGRATION_APPEAL_ADDITIONAL_INFORMATION),
            Arguments.of(FOREIGN_NATIONAL_OFFENDER, CASE_FLAG_FOREIGN_NATIONAL_OFFENDER_EXISTS, CASE_FLAG_FOREIGN_NATIONAL_OFFENDER_ADDITIONAL_INFORMATION),
            Arguments.of(POTENTIALLY_VIOLENT_PERSON, CASE_FLAG_POTENTIALLY_VIOLENT_PERSON_EXISTS, CASE_FLAG_POTENTIALLY_VIOLENT_PERSON_ADDITIONAL_INFORMATION),
            Arguments.of(UNACCEPTABLE_CUSTOMER_BEHAVIOUR, CASE_FLAG_UNACCEPTABLE_CUSTOMER_BEHAVIOUR_EXISTS, CASE_FLAG_UNACCEPTABLE_CUSTOMER_BEHAVIOUR_ADDITIONAL_INFORMATION),
            Arguments.of(UNACCOMPANIED_MINOR, CASE_FLAG_UNACCOMPANIED_MINOR_EXISTS, CASE_FLAG_UNACCOMPANIED_MINOR_ADDITIONAL_INFORMATION)
        );
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> removeFlagHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> removeFlagHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> removeFlagHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> removeFlagHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
