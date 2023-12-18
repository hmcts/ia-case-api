package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.flagcase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGACY_CASE_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMOVE_FLAG_TYPE_OF_FLAG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.LegacyCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RemoveFlagHandlerTest {

    private final LegacyCaseFlag expectedCaseFlag =
        new LegacyCaseFlag(CaseFlagType.COMPLEX_CASE, "some complex flag additional info");
    private final LegacyCaseFlag expectedCaseFlag2 =
        new LegacyCaseFlag(CaseFlagType.ANONYMITY, "some anonymity flag additional info");
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    private List<IdValue<LegacyCaseFlag>> expectedList;
    private IdValue<LegacyCaseFlag> expectedIdValue;
    private RemoveFlagHandler removeFlagHandler;

    @BeforeEach
    public void setUp() {
        when(callback.getEvent()).thenReturn(Event.REMOVE_FLAG);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

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
            .map(idValue -> new Value(idValue.getId(), idValue.getValue().getLegacyCaseFlagType().getReadableText()))
            .collect(Collectors.toList());

        final DynamicList dynamicList = new DynamicList(expectedElements.get(0), expectedElements);
        final List<IdValue<LegacyCaseFlag>> expectedCaseFlagList = new ArrayList<>();
        expectedCaseFlagList.add(expectedIdValue);

        when(asylumCase.read(REMOVE_FLAG_TYPE_OF_FLAG, DynamicList.class)).thenReturn(Optional.of(dynamicList));
        when(asylumCase.read(LEGACY_CASE_FLAGS)).thenReturn(Optional.of(expectedList));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            removeFlagHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(REMOVE_FLAG_TYPE_OF_FLAG, DynamicList.class);
        verify(asylumCase, times(1)).read(LEGACY_CASE_FLAGS);

        verify(asylumCase, times(1)).write(LEGACY_CASE_FLAGS, expectedCaseFlagList);
        verify(asylumCase, times(1)).clear(REMOVE_FLAG_TYPE_OF_FLAG);
    }


    @ParameterizedTest
    @CsvSource({
        "ANONYMITY, CASE_FLAG_ANONYMITY_EXISTS, CASE_FLAG_ANONYMITY_ADDITIONAL_INFORMATION",
        "COMPLEX_CASE, CASE_FLAG_COMPLEX_CASE_EXISTS, CASE_FLAG_COMPLEX_CASE_ADDITIONAL_INFORMATION",
        "DEPORT, CASE_FLAG_DEPORT_EXISTS, CASE_FLAG_DEPORT_ADDITIONAL_INFORMATION",
        "DETAINED_IMMIGRATION_APPEAL, CASE_FLAG_DETAINED_IMMIGRATION_APPEAL_EXISTS, CASE_FLAG_DETAINED_IMMIGRATION_APPEAL_ADDITIONAL_INFORMATION",
        "FOREIGN_NATIONAL_OFFENDER, CASE_FLAG_FOREIGN_NATIONAL_OFFENDER_EXISTS, CASE_FLAG_FOREIGN_NATIONAL_OFFENDER_ADDITIONAL_INFORMATION",
        "POTENTIALLY_VIOLENT_PERSON, CASE_FLAG_POTENTIALLY_VIOLENT_PERSON_EXISTS, CASE_FLAG_POTENTIALLY_VIOLENT_PERSON_ADDITIONAL_INFORMATION",
        "UNACCEPTABLE_CUSTOMER_BEHAVIOUR, CASE_FLAG_UNACCEPTABLE_CUSTOMER_BEHAVIOUR_EXISTS, CASE_FLAG_UNACCEPTABLE_CUSTOMER_BEHAVIOUR_ADDITIONAL_INFORMATION",
        "UNACCOMPANIED_MINOR, CASE_FLAG_UNACCOMPANIED_MINOR_EXISTS, CASE_FLAG_UNACCOMPANIED_MINOR_ADDITIONAL_INFORMATION"
    })
    void clear_display_flags_test(CaseFlagType flagType,
                                         AsylumCaseFieldDefinition caseFlagExists,
                                         AsylumCaseFieldDefinition caseFlagAdditionalInformation) {
        removeFlagHandler.clearDisplayFlags(flagType, asylumCase);
        verify(asylumCase, times(1)).clear(caseFlagExists);
        verify(asylumCase, times(1)).clear(caseFlagAdditionalInformation);

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
