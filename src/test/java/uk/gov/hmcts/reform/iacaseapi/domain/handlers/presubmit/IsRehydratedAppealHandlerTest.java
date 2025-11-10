package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class IsRehydratedAppealHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private IsRehydratedAppealHandler isRehydratedAppealHandler;

    @BeforeEach
    void setup() {
        isRehydratedAppealHandler = new IsRehydratedAppealHandler();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
            "START_APPEAL", "EDIT_APPEAL"
    })
    void should_write_to_rehydrated_appeal_yes_if_source_of_appeal_rehydrated_appeal(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class)).thenReturn(Optional.of(SourceOfAppeal.REHYDRATED_APPEAL));
        when(asylumCase.read(IS_NOTIFICATION_TURNED_OFF, YesOrNo.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response = isRehydratedAppealHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, times(1)).write(IS_REHYDRATED_APPEAL, YesOrNo.YES);
        verify(asylumCase, times(1)).write(IS_NOTIFICATION_TURNED_OFF, YesOrNo.YES);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
            "START_APPEAL", "EDIT_APPEAL"
    })
    void should_write_to_rehydrated_appeal_no_if_source_of_appeal_paper_form(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class)).thenReturn(Optional.of(SourceOfAppeal.PAPER_FORM));

        PreSubmitCallbackResponse<AsylumCase> response = isRehydratedAppealHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, times(1)).write(IS_REHYDRATED_APPEAL, YesOrNo.NO);
        verify(asylumCase, times(0)).write(IS_NOTIFICATION_TURNED_OFF, YesOrNo.YES);
        verify(asylumCase, times(0)).write(IS_NOTIFICATION_TURNED_OFF, YesOrNo.NO);


    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
            "START_APPEAL", "EDIT_APPEAL"
    })
    void should_not_write_to_notification_turned_off_if_already_exists(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class)).thenReturn(Optional.of(SourceOfAppeal.REHYDRATED_APPEAL));
        when(asylumCase.read(IS_NOTIFICATION_TURNED_OFF, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> response = isRehydratedAppealHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, times(1)).write(IS_REHYDRATED_APPEAL, YesOrNo.YES);
        verify(asylumCase, times(0)).write(IS_NOTIFICATION_TURNED_OFF, YesOrNo.YES);
        verify(asylumCase, times(0)).write(IS_NOTIFICATION_TURNED_OFF, YesOrNo.NO);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> isRehydratedAppealHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> isRehydratedAppealHandler.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }
}
