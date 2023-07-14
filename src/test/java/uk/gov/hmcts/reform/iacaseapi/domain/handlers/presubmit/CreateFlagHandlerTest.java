package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CreateFlagHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private CreateFlagHandler createFlagHandler;

    private final String appellantNameForDisplay = "some-name";

    private final String witnessName = "witnessName";
    private final String witnessFamilyName = "witnessFamilyName";

    private final StrategicCaseFlag appellantCaseFlag = new StrategicCaseFlag(appellantNameForDisplay, StrategicCaseFlag.ROLE_ON_CASE_APPELLANT);
    private final StrategicCaseFlag strategicCaseFlagEmpty = new StrategicCaseFlag();

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CREATE_FLAG);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantNameForDisplay));

        when(asylumCase.read(WITNESS_DETAILS))
            .thenReturn(Optional.of(List.of(new IdValue<>(witnessName, new WitnessDetails(witnessName, witnessFamilyName)))));

        createFlagHandler = new CreateFlagHandler();
    }

    @Test
    void should_write_to_case_flag_fields() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            createFlagHandler.handle(ABOUT_TO_START, callback);

        verify(asylumCase, times(1))
            .write(APPELLANT_LEVEL_FLAGS, appellantCaseFlag);
        verify(asylumCase, times(1))
            .write(CASE_LEVEL_FLAGS, strategicCaseFlagEmpty);

        String witnessFullName = witnessName + " " + witnessFamilyName;
        List<IdValue<StrategicCaseFlag>> witnessCaseFlag = List
                .of(new IdValue<>(witnessFullName, new StrategicCaseFlag(witnessFullName, StrategicCaseFlag.ROLE_ON_CASE_WITNESS)));
        verify(asylumCase, times(1))
                .write(WITNESS_LEVEL_FLAGS, witnessCaseFlag);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> createFlagHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = createFlagHandler.canHandle(callbackStage, callback);

                if (event == Event.CREATE_FLAG
                    && callbackStage == ABOUT_TO_START) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> createFlagHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
