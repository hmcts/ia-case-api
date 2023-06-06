package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ProgressBarAdaSuffixAppenderTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private ProgressBarAdaSuffixAppender progressBarAdaSuffixAppender;

    @BeforeEach
    public void setUp() {

        progressBarAdaSuffixAppender = new ProgressBarAdaSuffixAppender();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_write_ada_suffix_if_appeal_is_ada() {

        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(asylumCase.read(IS_NABA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            progressBarAdaSuffixAppender.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(ADA_SUFFIX, "_ada");
    }

    private static Stream<Arguments> noAda() {
        return Stream.of(
            Arguments.of(Optional.of(NO), SUBMIT_APPEAL),
            Arguments.of(Optional.of(NO), TRANSFER_OUT_OF_ADA),
            // isAcceleratedDetainedAppeal is always set, but empty optional scenario is tested for code coverage
            Arguments.of(Optional.empty(), SUBMIT_APPEAL),
            Arguments.of(Optional.empty(), TRANSFER_OUT_OF_ADA)
        );
    }

    @ParameterizedTest
    @MethodSource("noAda")
    void should_write_blank_ada_suffix_if_appeal_is_not_ada(Optional<YesOrNo> isAcceleratedDetainedAppealOpt, Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_NABA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(isAcceleratedDetainedAppealOpt);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            progressBarAdaSuffixAppender.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        if (isAcceleratedDetainedAppealOpt.isPresent()) {
            verify(asylumCase, times(1)).write(ADA_SUFFIX, "");
        } else {
            verify(asylumCase, never()).write(ADA_SUFFIX, "_ada");
        }
    }

    @Test
    void should_write_suffix_for_out_of_ada_appeals_after_hearing_requirements() {

        when(callback.getEvent()).thenReturn(TRANSFER_OUT_OF_ADA);
        when(asylumCase.read(IS_NABA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class)).thenReturn(Optional.of(YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            progressBarAdaSuffixAppender.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(HEARING_REQ_SUFFIX, "_afterHearingReq");
    }

    private static Stream<Arguments> noSubmitHearingRequirements() {
        return Stream.of(
            Arguments.of(Optional.empty()),
            Arguments.of(Optional.of(NO))
        );
    }

    @ParameterizedTest
    @MethodSource("noSubmitHearingRequirements")
    void should_not_write_suffix_for_out_of_ada_appeals_after_hearing_requirements(Optional<YesOrNo> submitHearingRequirementsAvailable) {

        when(callback.getEvent()).thenReturn(TRANSFER_OUT_OF_ADA);
        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class)).thenReturn(submitHearingRequirementsAvailable);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            progressBarAdaSuffixAppender.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(HEARING_REQ_SUFFIX, "_afterHearingReq");
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = progressBarAdaSuffixAppender.canHandle(callbackStage, callback);

                if (Set.of(SUBMIT_APPEAL, TRANSFER_OUT_OF_ADA).contains(event)
                    && callbackStage == ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> progressBarAdaSuffixAppender.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> progressBarAdaSuffixAppender.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> progressBarAdaSuffixAppender.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> progressBarAdaSuffixAppender.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> progressBarAdaSuffixAppender.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_write_suffices_if_naba_disabled() {
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(asylumCase.read(IS_NABA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            progressBarAdaSuffixAppender.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(ADA_SUFFIX), any());
        verify(asylumCase, never()).write(eq(HEARING_REQ_SUFFIX), any());
    }
}

