package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.editdocs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.values;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class EditBailDocsMidEventHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Mock
    private DocumentWithMetadata legalRepDoc1;
    @Mock
    private Document document1;

    private EditBailDocsMidEventHandler editBailDocsMidEventHandler;

    @BeforeEach
    public void setUp() {

        editBailDocsMidEventHandler = new EditBailDocsMidEventHandler();

        when(callback.getEvent()).thenReturn(Event.EDIT_DOCUMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : values()) {

                boolean canHandle = editBailDocsMidEventHandler.canHandle(callbackStage, callback);

                if (event == Event.EDIT_DOCUMENTS
                    && callbackStage == MID_EVENT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> editBailDocsMidEventHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> editBailDocsMidEventHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> editBailDocsMidEventHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> editBailDocsMidEventHandler.canHandle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }


    @Test
    void should_successfully_validate_edit_documents() {

        DocumentWithMetadata validDocumentMetadata = new DocumentWithMetadata(document1, null, "22-07-2020", null);

        List<IdValue<DocumentWithMetadata>> legalRepDocs =
            Arrays.asList(
                new IdValue<>("1", legalRepDoc1),
                new IdValue<>("1", validDocumentMetadata)
            );
        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)).thenReturn(Optional.of(legalRepDocs));
        when(legalRepDoc1.getDocument()).thenReturn(document1);
        when(legalRepDoc1.getDateUploaded()).thenReturn("22-07-2020");

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editBailDocsMidEventHandler.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    void should_error_when_no_document_in_edited_list() {

        DocumentWithMetadata emptyDocumentMetadata = new DocumentWithMetadata(null, null, null, null);
        List<IdValue<DocumentWithMetadata>> legalRepDocs =
            Arrays.asList(
                new IdValue<>("1", legalRepDoc1),
                new IdValue<>("2", emptyDocumentMetadata)
            );
        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)).thenReturn(Optional.of(legalRepDocs));
        when(legalRepDoc1.getDocument()).thenReturn(document1);
        when(legalRepDoc1.getDateUploaded()).thenReturn("22-07-2020");

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editBailDocsMidEventHandler.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isNotEmpty();
        assertEquals(1, errors.size());
        assertTrue(errors.contains(
            "If you add a new document you must complete the fields related to that document including Date uploaded, or remove it, before you can submit your change."));

    }

    @Test
    void should_error_when_no_date_uploaded_in_edited_list() {

        DocumentWithMetadata emptyDocumentMetadata = new DocumentWithMetadata(document1, null, null, null);
        List<IdValue<DocumentWithMetadata>> legalRepDocs =
            Arrays.asList(
                new IdValue<>("1", legalRepDoc1),
                new IdValue<>("2", emptyDocumentMetadata)
            );
        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)).thenReturn(Optional.of(legalRepDocs));
        when(legalRepDoc1.getDocument()).thenReturn(document1);
        when(legalRepDoc1.getDateUploaded()).thenReturn("22-07-2020");

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editBailDocsMidEventHandler.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isNotEmpty();
        assertEquals(1, errors.size());
        assertTrue(errors.contains(
            "If you add a new document you must complete the fields related to that document including Date uploaded, or remove it, before you can submit your change."));

    }

}
