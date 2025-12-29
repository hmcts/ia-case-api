package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@MockitoSettings(strictness = Strictness.LENIENT)
public class DocumentsUploadedConfirmationTest {

    @Mock private Callback<BailCase> callback;

    @Mock private CaseDetails<BailCase> caseDetails;
    private DocumentsUploadedConfirmation documentsUploadedConfirmation;

    @BeforeEach
    public void setUp() {
        documentsUploadedConfirmation = new DocumentsUploadedConfirmation();
        when(callback.getEvent()).thenReturn(Event.UPLOAD_DOCUMENTS);
    }

    @Test
    void should_not_allow_null_args() {
        assertThatThrownBy(() -> documentsUploadedConfirmation.handle(null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_handle_invalid_callback() {
        when(callback.getEvent()).thenReturn(Event.MOVE_APPLICATION_TO_DECIDED); //Invalid event for this handler

        assertThatThrownBy(() -> documentsUploadedConfirmation.handle(callback)).isExactlyInstanceOf(
            IllegalStateException.class);
    }

    @Test
    void should_set_header_body() {
        long caseId = 1000L;
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);
        PostSubmitCallbackResponse response = documentsUploadedConfirmation.handle(callback);

        assertNotNull(response.getConfirmationBody(), "Confirmation Body is null");
        assertThat(response.getConfirmationBody().get()).contains("#### What happens next\n\n"
                                                                  + "The document(s) you uploaded are available to view"
                                                                  + " in the [documents tab](/cases/case-details/"
                                                                  + "1000#Documents).");

        assertNotNull(response.getConfirmationHeader(), "Confirmation Header is null");
        assertThat(response.getConfirmationHeader().get()).isEqualTo("# Your upload is complete");
    }
}
