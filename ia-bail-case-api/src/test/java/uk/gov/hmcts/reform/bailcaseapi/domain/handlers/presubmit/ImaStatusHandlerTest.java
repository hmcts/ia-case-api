package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ADMIN_HAS_IMA_STATUS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ADMIN_SELECT_IMA_STATUS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HO_SELECT_IMA_STATUS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Optional;
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
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImaStatusHandlerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;

    private ImaStatusHandler imaStatusHandler;

    @BeforeEach
    public void setUp() {
        imaStatusHandler = new ImaStatusHandler();
    }

    @ParameterizedTest
    @MethodSource("generateScenarios")
    void should_set_proper_has_ima_status_value(Optional<YesOrNo> hoSelectedIma, Optional<YesOrNo> adminSelectedIma) {
        when(callback.getEvent()).thenReturn(Event.IMA_STATUS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);

        when(bailCase.read(HO_SELECT_IMA_STATUS, YesOrNo.class)).thenReturn(hoSelectedIma);
        when(bailCase.read(ADMIN_SELECT_IMA_STATUS, YesOrNo.class)).thenReturn(adminSelectedIma);

        PreSubmitCallbackResponse<BailCase> response = imaStatusHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);

        if (hoSelectedIma.isEmpty()) {
            verify(bailCase, times(1)).write(ADMIN_HAS_IMA_STATUS, adminSelectedIma.get());
        } else {
            verify(bailCase, never()).write(ADMIN_HAS_IMA_STATUS, adminSelectedIma.get());
        }
    }

    private static Stream<Arguments> generateScenarios() {
        return Stream.of(
            Arguments.of(Optional.empty(), Optional.of(YesOrNo.YES)),
            Arguments.of(Optional.empty(), Optional.of(YesOrNo.NO)),
            Arguments.of(Optional.of(YesOrNo.YES), Optional.of(YesOrNo.NO))
        );
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> imaStatusHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> imaStatusHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> imaStatusHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> imaStatusHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> imaStatusHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.IMA_STATUS);
        assertThatThrownBy(() -> imaStatusHandler.handle(ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class);

    }
}
