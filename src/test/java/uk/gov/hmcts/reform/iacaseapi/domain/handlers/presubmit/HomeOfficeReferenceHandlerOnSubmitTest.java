package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.GWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
class HomeOfficeReferenceHandlerOnSubmitTest {

    private static final String HO_REFERENCE = "HO123456";

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    @InjectMocks
    private HomeOfficeReferenceHandlerOnSubmit handler;

    @BeforeEach
    void setUp() {
        // no-op
    }

    @Test
    void should_handle_about_to_submit_start_appeal() {

        when(callback.getEvent())
            .thenReturn(Event.START_APPEAL);

        assertEquals(
            true,
            handler.canHandle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            )
        );
    }

    @Test
    void should_handle_about_to_submit_edit_appeal() {

        when(callback.getEvent())
            .thenReturn(Event.EDIT_APPEAL);

        assertEquals(
            true,
            handler.canHandle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            )
        );
    }

    @Test
    void should_handle_about_to_submit_edit_appeal_after_submit() {

        when(callback.getEvent())
            .thenReturn(Event.EDIT_APPEAL_AFTER_SUBMIT);

        assertEquals(
            true,
            handler.canHandle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            )
        );
    }

    @Test
    void should_not_handle_wrong_stage() {

        assertFalse(
            handler.canHandle(
                PreSubmitCallbackStage.ABOUT_TO_START,
                callback
            )
        );
    }

    @Test
    void should_not_handle_wrong_event() {

        when(callback.getEvent())
            .thenReturn(Event.SUBMIT_APPEAL);

        assertFalse(
            handler.canHandle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            )
        );
    }

    @Test
    void should_throw_if_callback_stage_null() {

        assertThrows(
            NullPointerException.class,
            () -> handler.canHandle(null, callback)
        );
    }

    @Test
    void should_throw_if_callback_null() {

        assertThrows(
            NullPointerException.class,
            () -> handler.canHandle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                null
            )
        );
    }

    @Test
    void should_throw_if_cannot_handle() {

        when(callback.getEvent())
            .thenReturn(Event.SUBMIT_APPEAL);

        assertThrows(
            IllegalStateException.class,
            () -> handler.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            )
        );
    }

    @Test
    void should_return_response_without_writing_appellants_when_serialised_data_missing() {

        when(callback.getEvent())
            .thenReturn(Event.START_APPEAL);

        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.empty());

        when(asylumCase.read(
            HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
            String.class))
            .thenReturn(Optional.empty());

        when(callback.getCaseDetails())
            .thenReturn(caseDetails);

        when(caseDetails.getCaseData())
            .thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            );

        assertEquals(asylumCase, response.getData());

        verify(asylumCase, never())
            .write(eq(HOME_OFFICE_APPELLANTS), any());
    }

    @Test
    void should_deserialise_and_write_appellants_to_case() {

        when(callback.getEvent())
            .thenReturn(Event.START_APPEAL);

        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.empty());

        when(asylumCase.read(
            HOME_OFFICE_REFERENCE_NUMBER,
            String.class))
            .thenReturn(Optional.of(HO_REFERENCE));

        String json =
            "[{\"id\":\"1\",\"value\":{\"familyName\":\"Smith\"}}]";

        when(asylumCase.read(
            HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
            String.class))
            .thenReturn(Optional.of(json));

        when(callback.getCaseDetails())
            .thenReturn(caseDetails);

        when(caseDetails.getCaseData())
            .thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            );

        assertEquals(asylumCase, response.getData());

        verify(asylumCase)
            .write(
                eq(HOME_OFFICE_APPELLANTS),
                any(List.class)
            );
    }

    @Test
    void should_not_throw_when_serialised_json_invalid() {

        when(callback.getEvent())
            .thenReturn(Event.START_APPEAL);

        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.empty());

        when(asylumCase.read(
            HOME_OFFICE_REFERENCE_NUMBER,
            String.class))
            .thenReturn(Optional.of(HO_REFERENCE));

        when(asylumCase.read(
            HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
            String.class))
            .thenReturn(Optional.of("NOT VALID JSON"));

        when(callback.getCaseDetails())
            .thenReturn(caseDetails);

        when(caseDetails.getCaseData())
            .thenReturn(asylumCase);

        assertDoesNotThrow(
            () -> handler.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            )
        );

        verify(asylumCase, never())
            .write(eq(HOME_OFFICE_APPELLANTS), any());
    }

    @Test
    void should_correctly_deserialise_real_objects() {

        when(callback.getEvent())
            .thenReturn(Event.START_APPEAL);

        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.empty());

        when(asylumCase.read(
            HOME_OFFICE_REFERENCE_NUMBER,
            String.class))
            .thenReturn(Optional.of(HO_REFERENCE));

        when(callback.getCaseDetails())
            .thenReturn(caseDetails);

        when(caseDetails.getCaseData())
            .thenReturn(asylumCase);

        String json =
            "[{\"id\":\"123\",\"value\":{\"familyName\":\"Smith\",\"givenNames\":\"John\"}}]";

        when(asylumCase.read(
            HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
            String.class))
            .thenReturn(Optional.of(json));

        handler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );

        verify(asylumCase)
            .write(
                eq(HOME_OFFICE_APPELLANTS),
                any(List.class)
            );
    }

    @Test
    void should_throw_when_handle_called_with_wrong_stage() {

        IllegalStateException exception =
            assertThrows(
                IllegalStateException.class,
                () -> handler.handle(
                    PreSubmitCallbackStage.ABOUT_TO_START,
                    callback
                )
            );

        assertEquals(
            "Cannot handle callback",
            exception.getMessage()
        );
    }

    @Test
    void should_return_empty_response_when_serialised_value_blank() {

        when(callback.getEvent())
            .thenReturn(Event.START_APPEAL);

        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.empty());

        when(callback.getCaseDetails())
            .thenReturn(caseDetails);

        when(caseDetails.getCaseData())
            .thenReturn(asylumCase);

        when(asylumCase.read(
            HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
            String.class))
            .thenReturn(Optional.of(""));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            );

        assertEquals(asylumCase, response.getData());

        verify(asylumCase, never())
            .write(eq(HOME_OFFICE_APPELLANTS), any());
    }

    @Test
    void should_write_deserialised_appellants_with_expected_values() {

        when(callback.getEvent())
            .thenReturn(Event.START_APPEAL);

        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.empty());

        when(callback.getCaseDetails())
            .thenReturn(caseDetails);

        when(caseDetails.getCaseData())
            .thenReturn(asylumCase);

        when(asylumCase.read(
            HOME_OFFICE_REFERENCE_NUMBER,
            String.class))
            .thenReturn(Optional.of(HO_REFERENCE));

        String json =
            "[{\"id\":\"ABC123\",\"value\":{\"familyName\":\"Smith\",\"givenNames\":\"John\"}}]";

        when(asylumCase.read(
            HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
            String.class))
            .thenReturn(Optional.of(json));

        handler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );

        verify(asylumCase)
            .write(
                eq(HOME_OFFICE_APPELLANTS),
                argThat(
                    (List<IdValue<HomeOfficeAppellant>> appellants) ->
                        appellants.size() == 1
                            && "ABC123".equals(
                                appellants.getFirst().getId())
                            && "Smith".equals(
                                appellants.getFirst()
                                        .getValue()
                                        .getFamilyName())
                            && "John".equals(
                                appellants.getFirst()
                                        .getValue()
                                        .getGivenNames())
                )
            );
    }

    @Test
    void should_handle_malformed_json_exception_branch() {

        when(callback.getEvent())
            .thenReturn(Event.START_APPEAL);

        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.empty());

        when(callback.getCaseDetails())
            .thenReturn(caseDetails);

        when(caseDetails.getCaseData())
            .thenReturn(asylumCase);

        when(asylumCase.read(
            HOME_OFFICE_REFERENCE_NUMBER,
            String.class))
            .thenReturn(Optional.of(HO_REFERENCE));

        when(asylumCase.read(
            HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
            String.class))
            .thenReturn(Optional.of("{ definitely invalid json"));

        assertDoesNotThrow(
            () -> handler.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            )
        );

        verify(asylumCase, never())
            .write(eq(HOME_OFFICE_APPELLANTS), any());
    }

    @Test
    void should_not_deserialise_when_home_office_appellants_already_present() {

        when(callback.getEvent())
            .thenReturn(Event.START_APPEAL);

        when(callback.getCaseDetails())
            .thenReturn(caseDetails);

        when(caseDetails.getCaseData())
            .thenReturn(asylumCase);

        List<IdValue<HomeOfficeAppellant>> existingAppellants =
            List.of(new IdValue<>("1", new HomeOfficeAppellant()));

        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.of(existingAppellants));

        when(asylumCase.read(
            HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
            String.class))
            .thenReturn(Optional.of(
                "[{\"id\":\"2\",\"value\":{\"familyName\":\"Jones\"}}]"
            ));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            );

        assertEquals(asylumCase, response.getData());

        verify(asylumCase, never())
            .write(eq(HOME_OFFICE_APPELLANTS), any());
    }

    @Test
    void should_deserialise_using_gwf_reference_when_home_office_reference_missing() {

        when(callback.getEvent())
            .thenReturn(Event.START_APPEAL);

        when(callback.getCaseDetails())
            .thenReturn(caseDetails);

        when(caseDetails.getCaseData())
            .thenReturn(asylumCase);

        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.empty());

        when(asylumCase.read(
            HOME_OFFICE_REFERENCE_NUMBER,
            String.class))
            .thenReturn(Optional.empty());

        when(asylumCase.read(
            GWF_REFERENCE_NUMBER,
            String.class))
            .thenReturn(Optional.of("GWF-12345"));

        when(asylumCase.read(
            HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
            String.class))
            .thenReturn(Optional.of(
                "[{\"id\":\"1\",\"value\":{\"familyName\":\"Smith\"}}]"
            ));

        handler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );

        verify(asylumCase)
            .write(
                eq(HOME_OFFICE_APPELLANTS),
                any(List.class)
            );
    }

    @Test
    void should_throw_when_home_office_and_gwf_references_both_missing() {

        when(callback.getEvent())
            .thenReturn(Event.START_APPEAL);

        when(callback.getCaseDetails())
            .thenReturn(caseDetails);

        when(caseDetails.getCaseData())
            .thenReturn(asylumCase);

        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.empty());

        when(asylumCase.read(
            HOME_OFFICE_REFERENCE_NUMBER,
            String.class))
            .thenReturn(Optional.empty());

        when(asylumCase.read(
            GWF_REFERENCE_NUMBER,
            String.class))
            .thenReturn(Optional.empty());

        when(asylumCase.read(
            HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
            String.class))
            .thenReturn(Optional.of(
                "[{\"id\":\"1\",\"value\":{\"familyName\":\"Smith\"}}]"
            ));

        IllegalStateException exception =
            assertThrows(
                IllegalStateException.class,
                () -> handler.handle(
                    PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                    callback
                )
            );

        assertEquals(
            "homeOfficeReferenceNumber and gwfReferenceNumber are both missing - one or other is needed",
            exception.getMessage()
        );
    }

    @Test
    void should_not_require_reference_numbers_when_appellants_already_exist() {

        when(callback.getEvent())
            .thenReturn(Event.START_APPEAL);

        when(callback.getCaseDetails())
            .thenReturn(caseDetails);

        when(caseDetails.getCaseData())
            .thenReturn(asylumCase);

        List<IdValue<HomeOfficeAppellant>> existingAppellants =
            List.of(new IdValue<>("1", new HomeOfficeAppellant()));

        when(asylumCase.read(HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.of(existingAppellants));

        when(asylumCase.read(
            HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
            String.class))
            .thenReturn(Optional.of(
                "[{\"id\":\"2\",\"value\":{\"familyName\":\"Jones\"}}]"
            ));

        assertDoesNotThrow(
            () -> handler.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            )
        );

        verify(asylumCase, never())
            .write(eq(HOME_OFFICE_APPELLANTS), any());
    }


}