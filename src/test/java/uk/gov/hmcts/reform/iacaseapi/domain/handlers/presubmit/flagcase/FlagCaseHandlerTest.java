package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.flagcase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
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
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@RunWith(JUnitParamsRunner.class)
public class FlagCaseHandlerTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.LENIENT);

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseFlagAppender caseFlagAppender;
    @Mock
    private FeatureToggler featureToggler;

    private final String additionalInformation = "some additional information";
    private FlagCaseHandler flagCaseHandler;

    @Before
    public void setUp() {
        when(callback.getEvent()).thenReturn(Event.FLAG_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FLAG_CASE_ADDITIONAL_INFORMATION, String.class)).thenReturn(Optional.of(additionalInformation));

        flagCaseHandler = new FlagCaseHandler(caseFlagAppender, featureToggler);
    }

    @Test
    @Parameters({
        "ANONYMITY, CASE_FLAG_ANONYMITY_EXISTS, CASE_FLAG_ANONYMITY_ADDITIONAL_INFORMATION",
        "COMPLEX_CASE, CASE_FLAG_COMPLEX_CASE_EXISTS, CASE_FLAG_COMPLEX_CASE_ADDITIONAL_INFORMATION",
        "DEPORT, CASE_FLAG_DEPORT_EXISTS, CASE_FLAG_DEPORT_ADDITIONAL_INFORMATION",
        "DETAINED_IMMIGRATION_APPEAL, CASE_FLAG_DETAINED_IMMIGRATION_APPEAL_EXISTS, CASE_FLAG_DETAINED_IMMIGRATION_APPEAL_ADDITIONAL_INFORMATION",
        "FOREIGN_NATIONAL_OFFENDER, CASE_FLAG_FOREIGN_NATIONAL_OFFENDER_EXISTS, CASE_FLAG_FOREIGN_NATIONAL_OFFENDER_ADDITIONAL_INFORMATION",
        "POTENTIALLY_VIOLENT_PERSON, CASE_FLAG_POTENTIALLY_VIOLENT_PERSON_EXISTS, CASE_FLAG_POTENTIALLY_VIOLENT_PERSON_ADDITIONAL_INFORMATION",
        "UNACCEPTABLE_CUSTOMER_BEHAVIOUR, CASE_FLAG_UNACCEPTABLE_CUSTOMER_BEHAVIOUR_EXISTS, CASE_FLAG_UNACCEPTABLE_CUSTOMER_BEHAVIOUR_ADDITIONAL_INFORMATION",
        "UNACCOMPANIED_MINOR, CASE_FLAG_UNACCOMPANIED_MINOR_EXISTS, CASE_FLAG_UNACCOMPANIED_MINOR_ADDITIONAL_INFORMATION",
        "SET_ASIDE_REHEARD, CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, CASE_FLAG_SET_ASIDE_REHEARD_ADDITIONAL_INFORMATION",
    })
    public void given_flag_type_should_set_correct_flag(CaseFlagType caseFlagType,
                                                        AsylumCaseFieldDefinition caseFlagExists,
                                                        AsylumCaseFieldDefinition caseFlagAdditionalInformation) {
        final List<IdValue<CaseFlag>> existingCaseFlags = new ArrayList<>();
        final List<IdValue<CaseFlag>> allCaseFlags = new ArrayList<>();

        when(asylumCase.read(FLAG_CASE_TYPE_OF_FLAG, CaseFlagType.class)).thenReturn(Optional.of(caseFlagType));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            flagCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        readAndClearInitialCaseFlagsTest();

        verify(caseFlagAppender, times(1))
            .append(existingCaseFlags, caseFlagType, additionalInformation);

        verify(asylumCase, times(1)).write(CASE_FLAGS, allCaseFlags);
        verify(asylumCase, times(1)).write(caseFlagExists, YesOrNo.YES);
        verify(asylumCase, times(1)).write(caseFlagAdditionalInformation, additionalInformation);
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
        verify(asylumCase, never()).write(any(AsylumCaseFieldDefinition.class), eq(YesOrNo.YES));
    }

    @Test
    public void should_set_feature_flag_to_yes_when_set_aside_reheard_flag_given() {

        final List<IdValue<CaseFlag>> existingCaseFlags = new ArrayList<>();
        final List<IdValue<CaseFlag>> allCaseFlags = new ArrayList<>();
        final CaseFlagType caseFlagType = CaseFlagType.SET_ASIDE_REHEARD;

        when(asylumCase.read(FLAG_CASE_TYPE_OF_FLAG, CaseFlagType.class)).thenReturn(Optional.of(caseFlagType));
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);

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
        verify(asylumCase, times(1)).write(IS_REHEARD_APPEAL_ENABLED, YesOrNo.YES);
    }

    @Test
    public void should_set_feature_flag_to_no_when_set_aside_reheard_flag_given() {

        final List<IdValue<CaseFlag>> existingCaseFlags = new ArrayList<>();
        final List<IdValue<CaseFlag>> allCaseFlags = new ArrayList<>();
        final CaseFlagType caseFlagType = CaseFlagType.SET_ASIDE_REHEARD;

        when(asylumCase.read(FLAG_CASE_TYPE_OF_FLAG, CaseFlagType.class)).thenReturn(Optional.of(caseFlagType));
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(false);

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
        verify(asylumCase, times(1)).write(IS_REHEARD_APPEAL_ENABLED, YesOrNo.NO);
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
