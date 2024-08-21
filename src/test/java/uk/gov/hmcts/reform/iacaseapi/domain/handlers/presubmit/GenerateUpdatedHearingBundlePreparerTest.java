package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentGenerator;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class GenerateUpdatedHearingBundlePreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DocumentGenerator<AsylumCase> documentGenerator;

    private GenerateUpdatedHearingBundlePreparer generateUpdatedHearingBundlePreparer;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        generateUpdatedHearingBundlePreparer =
            new GenerateUpdatedHearingBundlePreparer(documentGenerator);
    }

    @Test
    void should_call_document_generator() {

        when(callback.getEvent()).thenReturn(Event.GENERATE_UPDATED_HEARING_BUNDLE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(documentGenerator.aboutToStart(callback)).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.STATE_BEFORE_ADJOURN_WITHOUT_DATE, String.class))
            .thenReturn(Optional.of(State.PRE_HEARING.toString()));
        generateUpdatedHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(documentGenerator).aboutToStart(callback);
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {"PRE_HEARING", "DECISION"})
    void should_call_when_adjourned_from_valid_state(State adjournedFromState) {

        when(callback.getEvent()).thenReturn(Event.GENERATE_UPDATED_HEARING_BUNDLE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.ADJOURNED);
        when(asylumCase.read(AsylumCaseFieldDefinition.STATE_BEFORE_ADJOURN_WITHOUT_DATE, String.class))
            .thenReturn(Optional.of(adjournedFromState.toString()));
        when(documentGenerator.aboutToStart(callback)).thenReturn(asylumCase);

        generateUpdatedHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(documentGenerator).aboutToStart(callback);
    }

    @ParameterizedTest
    @EnumSource(value = State.class, mode = EXCLUDE, names = {"PRE_HEARING", "DECISION"})
    void should_call_when_adjourned_from_invalid_state(State adjournedFromState) {

        when(callback.getEvent()).thenReturn(Event.GENERATE_UPDATED_HEARING_BUNDLE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.ADJOURNED);
        when(asylumCase.read(AsylumCaseFieldDefinition.STATE_BEFORE_ADJOURN_WITHOUT_DATE, String.class))
            .thenReturn(Optional.of(adjournedFromState.toString()));
        when(documentGenerator.aboutToStart(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> response =
            generateUpdatedHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(documentGenerator, times(0)).aboutToStart(callback);
        assertTrue(response.getErrors()
            .contains("Case was adjourned before the initial hearing bundle was created."));
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = generateUpdatedHearingBundlePreparer.canHandle(callbackStage, callback);

                if (event == Event.GENERATE_UPDATED_HEARING_BUNDLE
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

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

        assertThatThrownBy(() -> generateUpdatedHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }


    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> generateUpdatedHearingBundlePreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generateUpdatedHearingBundlePreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generateUpdatedHearingBundlePreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generateUpdatedHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

}
