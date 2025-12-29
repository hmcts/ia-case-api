package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@MockitoSettings(strictness = Strictness.LENIENT)
class EditBailApplicationConfirmationTest {

    @Mock private Callback<BailCase> callback;

    @Mock private CaseDetails<BailCase> caseDetails;
    private EditBailApplicationConfirmation editBailApplicationConfirmation;

    @BeforeEach
    public void setUp() {
        editBailApplicationConfirmation = new EditBailApplicationConfirmation();
        when(callback.getEvent()).thenReturn(Event.EDIT_BAIL_APPLICATION);
    }

    @Test
    void should_set_header_body() {
        long caseId = 1234L;
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);
        PostSubmitCallbackResponse response = editBailApplicationConfirmation.handle(callback);

        assertNotNull(response.getConfirmationBody(), "Confirmation Body is null");
        assertThat(response.getConfirmationBody().get()).contains("# Do this next");

        assertNotNull(response.getConfirmationHeader(), "Confirmation Header is null");
        assertThat(response.getConfirmationHeader().get()).isEqualTo("# Your application details have been updated");
    }

    @Test
    void should_not_allow_null_args() {
        assertThatThrownBy(() -> editBailApplicationConfirmation.handle(null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_handle_invalid_callback() {
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION); //Invalid event for this handler

        assertThatThrownBy(() -> editBailApplicationConfirmation.handle(callback)).isExactlyInstanceOf(
            IllegalStateException.class);
    }

    @Test
    void should_return_confirmation_on_edit_application_save() {
        long caseId = 1234L;
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);

        PostSubmitCallbackResponse response = editBailApplicationConfirmation.handle(callback);

        assertNotNull(response);
        assertThat(response.getConfirmationBody().isPresent());
        assertThat(response.getConfirmationHeader().isPresent());

        assertThat(response.getConfirmationBody().get()).contains(
            "You still need to [submit the application](/case/IA/Bail/"
            + caseId
            + "/trigger/submitApplication). If you need to make further changes you can [edit the application]"
            + "(/case/IA/Bail/"
            + caseId
            + "/trigger/editBailApplication).");
    }

}
