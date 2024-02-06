package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class HearingTypeHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Captor
    private ArgumentCaptor<YesOrNo> hearingTypeResult;

    @Captor
    private ArgumentCaptor<AsylumCaseFieldDefinition> asylumExtractor;

    private final String isAcc = "Yes";

    private HearingTypeHandler hearingTypeHandler;

    @BeforeEach
    public void setUp() {
        hearingTypeHandler = new HearingTypeHandler();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, String.class)).thenReturn(Optional.of(isAcc));
    }

    @ParameterizedTest
    @ValueSource(strings = {"DC", "RP"})
    void should_write_to_hearing_type_result_yes_for_edit_appeal_event_ada(String type) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE_FOR_DISPLAY, AppealTypeForDisplay.class)).thenReturn(Optional.of(AppealTypeForDisplay.valueOf(type)));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        hearingTypeHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        verify(asylumCase, times(1))
                .write(AsylumCaseFieldDefinition.HEARING_TYPE_RESULT, YesOrNo.YES);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DC", "RP"})
    void should_write_to_hearing_type_result_yes_for_edit_appeal_event_detained_not_ada(String type) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE_FOR_DISPLAY, AppealTypeForDisplay.class)).thenReturn(Optional.of(AppealTypeForDisplay.valueOf(type)));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        hearingTypeHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        verify(asylumCase, times(1))
                .write(AsylumCaseFieldDefinition.HEARING_TYPE_RESULT, YesOrNo.YES);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DC", "RP"})
    void should_write_to_hearing_type_result_yes_for_edit_appeal_event_not_detained_not_ada(String type) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE_FOR_DISPLAY, AppealTypeForDisplay.class)).thenReturn(Optional.of(AppealTypeForDisplay.valueOf(type)));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        hearingTypeHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        verify(asylumCase, times(1))
                .write(AsylumCaseFieldDefinition.HEARING_TYPE_RESULT, YesOrNo.YES);
    }

    @ParameterizedTest
    @ValueSource(strings = {"PA", "EA", "HU"})
    void should_write_to_hearing_type_result_no_for_edit_appeal_event(String type) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.valueOf(type)));

        hearingTypeHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        verify(asylumCase, times(1))
                .write(AsylumCaseFieldDefinition.HEARING_TYPE_RESULT, YesOrNo.NO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DC", "RP"})
    void should_write_to_hearing_type_result_yes_for_start_appeal_event(String type) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(APPEAL_TYPE_FOR_DISPLAY, AppealTypeForDisplay.class))
                .thenReturn(Optional.of(AppealTypeForDisplay.valueOf(type)));

        hearingTypeHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        verify(asylumCase, times(1))
                .write(AsylumCaseFieldDefinition.HEARING_TYPE_RESULT, YesOrNo.YES);
    }

    @ParameterizedTest
    @ValueSource(strings = {"PA", "EA", "HU"})
    void should_write_to_hearing_type_result_no_for_start_appeal_event(String type) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(APPEAL_TYPE_FOR_DISPLAY, AppealTypeForDisplay.class))
                .thenReturn(Optional.of(AppealTypeForDisplay.valueOf(type)));

        hearingTypeHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        verify(asylumCase, times(1))
                .write(AsylumCaseFieldDefinition.HEARING_TYPE_RESULT, YesOrNo.NO);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> hearingTypeHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> hearingTypeHandler.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = hearingTypeHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.MID_EVENT && (event.equals(Event.START_APPEAL) || event.equals(Event.EDIT_APPEAL))) {

                    assertThat(canHandle).isEqualTo(true);
                } else {
                    assertThat(canHandle).isEqualTo(false);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> hearingTypeHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> hearingTypeHandler.canHandle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

    @Test
    void should_set_hearing_type_for_age_assessment_start_appeal() {
        when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        hearingTypeHandler.handle(MID_EVENT, callback);

        verify(asylumCase, times(1)).write(asylumExtractor.capture(), hearingTypeResult.capture());
        assertThat(asylumExtractor.getValue()).isEqualTo(HEARING_TYPE_RESULT);
        assertThat(hearingTypeResult.getValue()).isEqualTo(YesOrNo.NO);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL", "EDIT_APPEAL"
    })
    void should_set_hearing_type_no_fee_for_ejp_cases(Event event) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class))
            .thenReturn(Optional.of(SourceOfAppeal.TRANSFERRED_FROM_UPPER_TRIBUNAL));
        when(asylumCase.read(APPEAL_TYPE_FOR_DISPLAY, AppealTypeForDisplay.class))
            .thenReturn(Optional.of(AppealTypeForDisplay.HU));

        hearingTypeHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        verify(asylumCase, times(1))
            .write(AsylumCaseFieldDefinition.HEARING_TYPE_RESULT, YesOrNo.YES);
    }
}
