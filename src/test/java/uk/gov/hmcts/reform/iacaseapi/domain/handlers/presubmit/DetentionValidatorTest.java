package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL_AFTER_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class DetentionValidatorTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private final DetentionValidator detentionValidator = new DetentionValidator();

    @BeforeEach
    void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getPageId()).thenReturn("detention");
    }

    @Test
    void should_add_error_to_asylum_case_if_admin_starts_non_detained_appeal() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION, YesOrNo.class))
            .thenReturn(Optional.of(NO));
        when(asylumCase.read(AsylumCaseFieldDefinition.IS_ADMIN, YesOrNo.class))
            .thenReturn(Optional.of(YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = detentionValidator
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isNotEmpty();
        assertEquals(1, errors.size());
        assertTrue(errors.contains("The option is currently unavailable"));
    }

    private static Stream<Arguments> noErrorScenarios() {
        // isAdmin, appellantInDetention
        return Stream.of(
            Arguments.of(NO, NO),
            Arguments.of(YES, YES),
            Arguments.of(NO, YES)
        );
    }

    @ParameterizedTest
    @MethodSource("noErrorScenarios")
    void should_not_add_error_to_asylum_case_if_admin_starts_non_detained_appeal(YesOrNo isAdmin, YesOrNo appellantInDetention) {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION, YesOrNo.class))
            .thenReturn(Optional.of(appellantInDetention));
        when(asylumCase.read(AsylumCaseFieldDefinition.IS_ADMIN, YesOrNo.class))
            .thenReturn(Optional.of(isAdmin));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = detentionValidator
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(
            () -> detentionValidator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = detentionValidator.canHandle(callbackStage, callback);
                if (callbackStage == PreSubmitCallbackStage.MID_EVENT
                    && Set.of(START_APPEAL,
                    EDIT_APPEAL,
                    EDIT_APPEAL_AFTER_SUBMIT).contains(callback.getEvent())) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> detentionValidator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> detentionValidator.canHandle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> detentionValidator.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> detentionValidator.handle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
