package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class TransferOutOfAdaHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider dateProvider;

    private TransferOutOfAdaHandler transferOutOfAdaHandler;

    private LocalDate date = LocalDate.now();

    @BeforeEach
    public void setup() {

        when(dateProvider.now()).thenReturn(date);
        transferOutOfAdaHandler = new TransferOutOfAdaHandler(dateProvider);
    }

    @Test
    void transferring_from_ada_to_non_ada_and_updating_case_data() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getEvent()).thenReturn(Event.TRANSFER_OUT_OF_ADA);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                transferOutOfAdaHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.NO);
        verify(asylumCase).write(DETENTION_STATUS, DetentionStatus.DETAINED);
        verify(asylumCase).write(TRANSFER_OUT_OF_ADA_DATE, dateProvider.now().toString());
        verify(asylumCase).write(HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.YES);

        verify(asylumCase).write(LISTING_AVAILABLE_FOR_ADA, YesOrNo.NO);
        verify(asylumCase).write(ADA_HEARING_ADJUSTMENTS_UPDATABLE, YesOrNo.NO);
        verify(asylumCase).write(ADA_HEARING_REQUIREMENTS_UPDATABLE, YesOrNo.NO);
        verify(asylumCase).write(ADA_HEARING_REQUIREMENTS_TO_REVIEW, YesOrNo.NO);
        verify(asylumCase).write(ADA_HEARING_REQUIREMENTS_SUBMITTABLE, YesOrNo.NO);
        verify(asylumCase).write(ADA_EDIT_LISTING_AVAILABLE, YesOrNo.NO);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(
                () -> transferOutOfAdaHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = transferOutOfAdaHandler.canHandle(callbackStage, callback);
                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && ((callback.getEvent() == Event.TRANSFER_OUT_OF_ADA))) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> transferOutOfAdaHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
                () -> transferOutOfAdaHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> transferOutOfAdaHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> transferOutOfAdaHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

}