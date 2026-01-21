package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
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
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdSupplementaryUpdater;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BailApplicationSavedConfirmationTest {

    @Mock private Callback<BailCase> callback;

    @Mock private CaseDetails<BailCase> caseDetails;

    @Mock private BailCase bailCase;

    @Mock
    private CcdSupplementaryUpdater ccdSupplementaryUpdater;

    private BailApplicationSavedConfirmation bailApplicationSavedConfirmation =
        new BailApplicationSavedConfirmation(ccdSupplementaryUpdater);

    @BeforeEach
    public void setUp() {
        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        bailApplicationSavedConfirmation = new BailApplicationSavedConfirmation(ccdSupplementaryUpdater);
    }

    @Test
    void should_invoke_supplementary_updater() {
        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);

        PostSubmitCallbackResponse response = bailApplicationSavedConfirmation.handle(callback);

        verify(ccdSupplementaryUpdater).setHmctsServiceIdSupplementary(callback);
        if (response.getConfirmationBody().isEmpty()) {
            throw new IllegalStateException("Response body is not present");
        }
        String responseBody = response.getConfirmationBody().get();
        assertThat(responseBody).contains("### Do this next");
        assertThat(responseBody).contains("If you're ready to submit your application, " +
                                              "select 'Submit your application' in the 'Next step' dropdown " +
                                              "list from your case details page.");
        assertThat(responseBody).contains("#### Not ready to submit your application yet?");
        assertThat(responseBody).contains("You can return to the case details page to make changes " +
                                              "from the ‘Next step’ dropdown list.");

    }

    @Test
    void should_set_header_body() {
        long caseId = 1234L;

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);

        PostSubmitCallbackResponse response = bailApplicationSavedConfirmation.handle(callback);

        assertNotNull(response.getConfirmationBody(), "Confirmation Body is null");
        assertThat(response.getConfirmationBody().get()).contains("# Do this next");

        assertNotNull(response.getConfirmationHeader(), "Confirmation Header is null");
        assertThat(response.getConfirmationHeader().get()).isEqualTo("# You have saved this application");
    }

    @Test
    void should_not_allow_null_args() {
        assertThatThrownBy(() -> bailApplicationSavedConfirmation.handle(null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_handle_invalid_callback() {
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION); //Invalid event for this handler

        assertThatThrownBy(() -> bailApplicationSavedConfirmation.handle(callback)).isExactlyInstanceOf(
            IllegalStateException.class);
    }
}
