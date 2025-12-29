package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.UpdateInterpreterDetailsHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.INTERPRETER_DETAILS;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UpdateInterpreterDetailsHandlerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;

    private UpdateInterpreterDetailsHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UpdateInterpreterDetailsHandler();
        when(callback.getEvent()).thenReturn(Event.UPDATE_INTERPRETER_DETAILS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
    }

    @Test
    void testCanHandle() {
        when(callback.getEvent()).thenReturn(Event.UPDATE_INTERPRETER_DETAILS);
        boolean canHandle = handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertTrue(canHandle);
    }

    @Test
    public void testHandleInterpreterDetails() {
        InterpreterDetails interpreterWithId = InterpreterDetails.builder()
            .interpreterId("1")
            .interpreterBookingRef("ref1")
            .interpreterGivenNames("John")
            .interpreterFamilyName("Smith")
            .interpreterPhoneNumber("123")
            .interpreterEmail("email")
            .interpreterNote("note")
            .build();

        InterpreterDetails interpreterWithoutId = InterpreterDetails.builder()
            .interpreterId(null)
            .interpreterBookingRef("ref2")
            .interpreterGivenNames("Jane")
            .interpreterFamilyName("Smith")
            .interpreterPhoneNumber("456")
            .interpreterEmail("email2")
            .interpreterNote("note2")
            .build();

        List<IdValue<InterpreterDetails>> interpreterDetailsList = new ArrayList<>();
        interpreterDetailsList.add(new IdValue<>("1", interpreterWithId));
        interpreterDetailsList.add(new IdValue<>("2", interpreterWithoutId));

        // Given that the case has interpreter details, one with an id and one without
        when(bailCase.read(eq(INTERPRETER_DETAILS))).thenReturn(Optional.of(interpreterDetailsList));

        PreSubmitCallbackResponse<BailCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);

        List<IdValue<InterpreterDetails>> updatedInterpreterDetails =
            ((List<IdValue<InterpreterDetails>>) response.getData().read(INTERPRETER_DETAILS)
                .orElse(Collections.emptyList()));

        // Verify that all the interpreter have an id
        assertTrue(updatedInterpreterDetails.stream()
            .noneMatch(idValue -> isEmpty(idValue.getValue().getInterpreterId())));
    }

}
