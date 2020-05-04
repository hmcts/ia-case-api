package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.flagcase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CaseFlagAppender;

@RunWith(MockitoJUnitRunner.class)
public class FlagCaseHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private CaseFlagAppender caseFlagAppender;

    @Captor private ArgumentCaptor<List<IdValue<CaseFlag>>> existingCaseFlagsCaptor;

    private final String additionalInformation = "some additional information";

    private FlagCaseHandler flagCaseHandler;

    @Before
    public void setUp() {
        when(callback.getEvent()).thenReturn(Event.FLAG_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FLAG_CASE_ADDITIONAL_INFORMATION, String.class)).thenReturn(Optional.of(additionalInformation));

        flagCaseHandler = new FlagCaseHandler(caseFlagAppender);
    }

    @Test
    public void set_correct_flags_when_flag_type_is_anonymity() {

        final List<IdValue<CaseFlag>> existingCaseFlags = new ArrayList<>();
        final List<IdValue<CaseFlag>> allCaseFlags = new ArrayList<>();
        final CaseFlagType caseFlagType = CaseFlagType.ANONYMITY;

        when(asylumCase.read(FLAG_CASE_TYPE_OF_FLAG, CaseFlagType.class)).thenReturn(Optional.of(caseFlagType));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            flagCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        readAndClearInitialCaseFlagsTest();

        verify(caseFlagAppender, times(1)).append(
            existingCaseFlags,
            caseFlagType,
            additionalInformation
        );

        verify(asylumCase, times(1)).write(CASE_FLAGS, allCaseFlags);
        verify(asylumCase, times(1)).write(CASE_FLAG_ANONYMITY_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(1)).write(CASE_FLAG_ANONYMITY_ADDITIONAL_INFORMATION, additionalInformation);
    }

    @Test
    public void set_correct_flags_when_flag_type_is_complex_case() {

        final List<IdValue<CaseFlag>> existingCaseFlags = new ArrayList<>();
        final List<IdValue<CaseFlag>> allCaseFlags = new ArrayList<>();
        final CaseFlagType caseFlagType = CaseFlagType.COMPLEX_CASE;

        when(asylumCase.read(FLAG_CASE_TYPE_OF_FLAG, CaseFlagType.class)).thenReturn(Optional.of(caseFlagType));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            flagCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        readAndClearInitialCaseFlagsTest();

        verify(caseFlagAppender, times(1)).append(
            existingCaseFlags,
            caseFlagType,
            additionalInformation
        );

        verify(asylumCase, times(1)).write(CASE_FLAGS, allCaseFlags);
        verify(asylumCase, times(1)).write(CASE_FLAG_COMPLEX_CASE_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(1)).write(CASE_FLAG_COMPLEX_CASE_ADDITIONAL_INFORMATION, additionalInformation);
    }

    @Test
    public void set_correct_flags_when_flag_type_is_detained_immigrations_appeal() {

        final List<IdValue<CaseFlag>> existingCaseFlags = new ArrayList<>();
        final List<IdValue<CaseFlag>> allCaseFlags = new ArrayList<>();
        final CaseFlagType caseFlagType = CaseFlagType.DETAINED_IMMIGRATION_APPEAL;

        when(asylumCase.read(FLAG_CASE_TYPE_OF_FLAG, CaseFlagType.class)).thenReturn(Optional.of(caseFlagType));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            flagCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        readAndClearInitialCaseFlagsTest();

        verify(caseFlagAppender, times(1)).append(
            existingCaseFlagsCaptor.capture(),
            eq(caseFlagType),
            eq(additionalInformation)
        );

        verify(asylumCase, times(1)).write(CASE_FLAGS, allCaseFlags);
        verify(asylumCase, times(1)).write(CASE_FLAG_DETAINED_IMMIGRATION_APPEAL_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(1)).write(CASE_FLAG_DETAINED_IMMIGRATION_APPEAL_ADDITIONAL_INFORMATION, additionalInformation);
    }

    @Test
    public void set_correct_flags_when_flag_type_is_foreign_national_offender() {

        final List<IdValue<CaseFlag>> existingCaseFlags = new ArrayList<>();
        final List<IdValue<CaseFlag>> allCaseFlags = new ArrayList<>();
        final CaseFlagType caseFlagType = CaseFlagType.FOREIGN_NATIONAL_OFFENDER;

        when(asylumCase.read(FLAG_CASE_TYPE_OF_FLAG, CaseFlagType.class)).thenReturn(Optional.of(caseFlagType));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            flagCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        readAndClearInitialCaseFlagsTest();

        verify(caseFlagAppender, times(1)).append(
            existingCaseFlags,
            caseFlagType,
            additionalInformation
        );

        verify(asylumCase, times(1)).write(CASE_FLAGS, allCaseFlags);
        verify(asylumCase, times(1)).write(CASE_FLAG_FOREIGN_NATIONAL_OFFENDER_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(1)).write(CASE_FLAG_FOREIGN_NATIONAL_OFFENDER_ADDITIONAL_INFORMATION, additionalInformation);
    }

    @Test
    public void set_correct_flags_when_flag_type_is_potentially_violent_person() {

        final List<IdValue<CaseFlag>> existingCaseFlags = new ArrayList<>();
        final List<IdValue<CaseFlag>> allCaseFlags = new ArrayList<>();
        final CaseFlagType caseFlagType = CaseFlagType.POTENTIALLY_VIOLENT_PERSON;

        when(asylumCase.read(FLAG_CASE_TYPE_OF_FLAG, CaseFlagType.class)).thenReturn(Optional.of(caseFlagType));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            flagCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        readAndClearInitialCaseFlagsTest();

        verify(caseFlagAppender, times(1)).append(
            existingCaseFlags,
            caseFlagType,
            additionalInformation
        );

        verify(asylumCase, times(1)).write(CASE_FLAGS, allCaseFlags);
        verify(asylumCase, times(1)).write(CASE_FLAG_POTENTIALLY_VIOLENT_PERSON_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(1)).write(CASE_FLAG_POTENTIALLY_VIOLENT_PERSON_ADDITIONAL_INFORMATION, additionalInformation);
    }

    @Test
    public void set_correct_flags_when_flag_type_is_unacceptable_customer_behaviour() {

        final List<IdValue<CaseFlag>> existingCaseFlags = new ArrayList<>();
        final List<IdValue<CaseFlag>> allCaseFlags = new ArrayList<>();
        final CaseFlagType caseFlagType = CaseFlagType.UNACCEPTABLE_CUSTOMER_BEHAVIOUR;

        when(asylumCase.read(FLAG_CASE_TYPE_OF_FLAG, CaseFlagType.class)).thenReturn(Optional.of(caseFlagType));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            flagCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        readAndClearInitialCaseFlagsTest();

        verify(caseFlagAppender, times(1)).append(
            existingCaseFlags,
            caseFlagType,
            additionalInformation
        );

        verify(asylumCase, times(1)).write(CASE_FLAGS, allCaseFlags);
        verify(asylumCase, times(1)).write(CASE_FLAG_UNACCEPTABLE_CUSTOMER_BEHAVIOUR_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(1)).write(CASE_FLAG_UNACCEPTABLE_CUSTOMER_BEHAVIOUR_ADDITIONAL_INFORMATION, additionalInformation);
    }

    @Test
    public void set_correct_flags_when_flag_type_is_unaccompanied_minor() {

        final List<IdValue<CaseFlag>> existingCaseFlags = new ArrayList<>();
        final List<IdValue<CaseFlag>> allCaseFlags = new ArrayList<>();
        final CaseFlagType caseFlagType = CaseFlagType.UNACCOMPANIED_MINOR;

        when(asylumCase.read(FLAG_CASE_TYPE_OF_FLAG, CaseFlagType.class)).thenReturn(Optional.of(caseFlagType));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            flagCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        readAndClearInitialCaseFlagsTest();

        verify(caseFlagAppender, times(1)).append(
            existingCaseFlags,
            caseFlagType,
            additionalInformation
        );

        verify(asylumCase, times(1)).write(CASE_FLAGS, allCaseFlags);
        verify(asylumCase, times(1)).write(CASE_FLAG_UNACCOMPANIED_MINOR_EXISTS, YesOrNo.YES);
        verify(asylumCase, times(1)).write(CASE_FLAG_UNACCOMPANIED_MINOR_ADDITIONAL_INFORMATION, additionalInformation);
    }

    @Test
    public void does_not_set_any_flags_when_bad_flag_given() {

        final List<IdValue<CaseFlag>> existingCaseFlags = new ArrayList<>();
        final List<IdValue<CaseFlag>> allCaseFlags = new ArrayList<>();
        final CaseFlagType caseFlagType = CaseFlagType.UNKNOWN;

        when(asylumCase.read(FLAG_CASE_TYPE_OF_FLAG, CaseFlagType.class)).thenReturn(Optional.of(caseFlagType));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            flagCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        readAndClearInitialCaseFlagsTest();

        verify(caseFlagAppender, times(1)).append(
            existingCaseFlags,
            caseFlagType,
            additionalInformation
        );

        verify(asylumCase, times(1)).write(CASE_FLAGS, allCaseFlags);
        verify(asylumCase, never()).write(CASE_FLAG_ANONYMITY_EXISTS, YesOrNo.YES);
        verify(asylumCase, never()).write(CASE_FLAG_ANONYMITY_ADDITIONAL_INFORMATION, additionalInformation);
        verify(asylumCase, never()).write(CASE_FLAG_COMPLEX_CASE_EXISTS, YesOrNo.YES);
        verify(asylumCase, never()).write(CASE_FLAG_COMPLEX_CASE_ADDITIONAL_INFORMATION, additionalInformation);
        verify(asylumCase, never()).write(CASE_FLAG_DETAINED_IMMIGRATION_APPEAL_EXISTS, YesOrNo.YES);
        verify(asylumCase, never()).write(CASE_FLAG_DETAINED_IMMIGRATION_APPEAL_ADDITIONAL_INFORMATION, additionalInformation);
        verify(asylumCase, never()).write(CASE_FLAG_FOREIGN_NATIONAL_OFFENDER_EXISTS, YesOrNo.YES);
        verify(asylumCase, never()).write(CASE_FLAG_FOREIGN_NATIONAL_OFFENDER_ADDITIONAL_INFORMATION, additionalInformation);
        verify(asylumCase, never()).write(CASE_FLAG_POTENTIALLY_VIOLENT_PERSON_EXISTS, YesOrNo.YES);
        verify(asylumCase, never()).write(CASE_FLAG_POTENTIALLY_VIOLENT_PERSON_ADDITIONAL_INFORMATION, additionalInformation);
        verify(asylumCase, never()).write(CASE_FLAG_UNACCEPTABLE_CUSTOMER_BEHAVIOUR_EXISTS, YesOrNo.YES);
        verify(asylumCase, never()).write(CASE_FLAG_UNACCEPTABLE_CUSTOMER_BEHAVIOUR_ADDITIONAL_INFORMATION, additionalInformation);
        verify(asylumCase, never()).write(CASE_FLAG_UNACCOMPANIED_MINOR_EXISTS, YesOrNo.YES);
        verify(asylumCase, never()).write(CASE_FLAG_UNACCOMPANIED_MINOR_ADDITIONAL_INFORMATION, additionalInformation);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> flagCaseHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> flagCaseHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> flagCaseHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> flagCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    public void readAndClearInitialCaseFlagsTest() {
        verify(asylumCase, times(1)).read(FLAG_CASE_TYPE_OF_FLAG, CaseFlagType.class);
        verify(asylumCase, times(1)).read(FLAG_CASE_ADDITIONAL_INFORMATION, String.class);

        verify(asylumCase, times(1)).clear(FLAG_CASE_TYPE_OF_FLAG);
        verify(asylumCase, times(1)).clear(FLAG_CASE_ADDITIONAL_INFORMATION);
    }
}
