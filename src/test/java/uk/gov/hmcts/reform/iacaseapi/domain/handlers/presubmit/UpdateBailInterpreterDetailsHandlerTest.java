package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.INTERPRETER_DETAILS;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UpdateBailInterpreterDetailsHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Mock
    private IaHearingsApiService iaHearingsApiService;
    private UpdateBailInterpreterDetailsHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UpdateBailInterpreterDetailsHandler(iaHearingsApiService);
        when(callback.getEvent()).thenReturn(Event.UPDATE_INTERPRETER_DETAILS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
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
        when(asylumCase.read(eq(INTERPRETER_DETAILS))).thenReturn(Optional.of(interpreterDetailsList));
        when(iaHearingsApiService.updateHearing(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);

        List<IdValue<InterpreterDetails>> updatedInterpreterDetails =
            ((List<IdValue<InterpreterDetails>>) response.getData().read(INTERPRETER_DETAILS)
                .orElse(Collections.emptyList()));

        // Verify that all the interpreter have an id
        assertTrue(updatedInterpreterDetails.stream()
            .noneMatch(idValue -> isEmpty(idValue.getValue().getInterpreterId())));

        verify(iaHearingsApiService, times(1)).updateHearing(callback);
    }

}
