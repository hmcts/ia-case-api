package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class IsEjpHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private IsEjpHandler isEjpHandler;

    @BeforeEach
    void setup() {
        isEjpHandler = new IsEjpHandler();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL", "EDIT_APPEAL"
    })
    void should_write_is_ejp_yes_for_transferred_from_ut(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class)).thenReturn(Optional.of(SourceOfAppeal.TRANSFERRED_FROM_UPPER_TRIBUNAL));

        PreSubmitCallbackResponse<AsylumCase> response = isEjpHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, times(1)).write(IS_EJP, YesOrNo.YES);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL", "EDIT_APPEAL"
    })
    void should_write_is_ejp_no_for_paper_form(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class)).thenReturn(Optional.of(SourceOfAppeal.PAPER_FORM));

        PreSubmitCallbackResponse<AsylumCase> response = isEjpHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, times(1)).write(IS_EJP, YesOrNo.NO);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class)
    void it_can_handle_callback(Event event) {

        for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
            when(callback.getEvent()).thenReturn(event);
            boolean canHandle = isEjpHandler.canHandle(stage, callback);

            if (stage == ABOUT_TO_SUBMIT && (event == Event.START_APPEAL || event == Event.EDIT_APPEAL)) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL", "EDIT_APPEAL"
    })
    void handling_should_throw_if_cannot_actually_handle(Event event) {
        when(callback.getEvent()).thenReturn(event);

        Assertions.assertThatThrownBy(() -> isEjpHandler.handle(MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        Assertions.assertThatThrownBy(() -> isEjpHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL", "EDIT_APPEAL"
    })
    void should_not_allow_null_arguments(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertThatThrownBy(() -> isEjpHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> isEjpHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> isEjpHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> isEjpHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}