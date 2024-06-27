package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AppealReferenceNumberCcdHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private AppealReferenceNumberCcdHandler appealReferenceNumberCcdHandler;

    @BeforeEach
    public void setUp() {

        appealReferenceNumberCcdHandler =
            new AppealReferenceNumberCcdHandler();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(123L);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
    }

    @Test
    void should_write_is_appeal_reference_number_available_field_when_appeal_reference_number_available_and_field_is_empty() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER)).thenReturn(Optional.of("some-existing-reference-number"));
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(IS_APPEAL_REFERENCE_NUMBER_AVAILABLE)).thenReturn(Optional.empty());

        appealReferenceNumberCcdHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(IS_APPEAL_REFERENCE_NUMBER_AVAILABLE, YesOrNo.YES);
    }

    @Test
    void should_not_write_is_appeal_reference_number_available_field_when_appeal_reference_number_is_draft() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER)).thenReturn(Optional.of("DRAFT"));
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(IS_APPEAL_REFERENCE_NUMBER_AVAILABLE)).thenReturn(Optional.empty());

        appealReferenceNumberCcdHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(0)).write(IS_APPEAL_REFERENCE_NUMBER_AVAILABLE, YesOrNo.YES);
    }

    @Test
    void should_not_write_to_is_appeal_reference_number_available_field_when_field_is_already_set_to_yes() {

        AsylumCase newAsylumCase = new AsylumCase();
        newAsylumCase.write(IS_APPEAL_REFERENCE_NUMBER_AVAILABLE, YesOrNo.YES);
        newAsylumCase.write(APPEAL_REFERENCE_NUMBER, "some-existing-reference-number");
        newAsylumCase.write(JOURNEY_TYPE, JourneyType.AIP);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(newAsylumCase);

        appealReferenceNumberCcdHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(0)).write(IS_APPEAL_REFERENCE_NUMBER_AVAILABLE, YesOrNo.YES);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(caseDetails.getCaseData()).thenReturn(asylumCase);
                when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

                boolean canHandle = appealReferenceNumberCcdHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && event == Event.SUBMIT_APPEAL
                ) {
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

        assertThatThrownBy(() -> appealReferenceNumberCcdHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
