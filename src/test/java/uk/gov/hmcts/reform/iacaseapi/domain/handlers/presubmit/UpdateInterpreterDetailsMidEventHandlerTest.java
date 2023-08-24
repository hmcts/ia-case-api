package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.INTERPRETER_DETAILS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class UpdateInterpreterDetailsMidEventHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private UpdateInterpreterDetailsMidEventHandler handler;


    @BeforeEach
    void setUp() {
        handler = new UpdateInterpreterDetailsMidEventHandler();
        when(callback.getEvent()).thenReturn(Event.UPDATE_INTERPRETER_DETAILS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void canHandle_shouldReturnTrueForMidEventAndUpdateInterpreterDetailsEvent() {
        boolean canHandle = handler.canHandle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertThat(canHandle).isTrue();
    }

    @Test
    void handle_shouldValidateInterpretersEmailAddresses() {
        InterpreterDetails interpreterWithValidEmail = InterpreterDetails.builder()
            .interpreterId("1")
            .interpreterBookingRef("ref1")
            .interpreterGivenNames("John")
            .interpreterFamilyName("Smith")
            .interpreterPhoneNumber("123")
            .interpreterEmail("validEmail@test.com")
            .interpreterNote("note")
            .build();

        InterpreterDetails interpreterWithInvalidEmail = InterpreterDetails.builder()
            .interpreterId("2")
            .interpreterBookingRef("ref2")
            .interpreterGivenNames("Jane")
            .interpreterFamilyName("Smith")
            .interpreterPhoneNumber("456")
            .interpreterEmail("invalid_email")
            .interpreterNote("note2")
            .build();

        List<IdValue<InterpreterDetails>> interpreterDetailsList = new ArrayList<>();
        interpreterDetailsList.add(new IdValue<>("1", interpreterWithValidEmail));
        interpreterDetailsList.add(new IdValue<>("2", interpreterWithInvalidEmail));

        // Given that the case has two interpreters, one with a valid email address and one with an invalid email address
        when(asylumCase.read(eq(INTERPRETER_DETAILS))).thenReturn(Optional.of(interpreterDetailsList));

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        // Verify error message is generated for the second interpreter who has an invalid email address
        assertThat(response.getErrors().iterator().next())
            .isEqualTo("Interpreter 2 email address is invalid, please enter a valid email address.");
    }
}