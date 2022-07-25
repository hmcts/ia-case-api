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
public class BailApplicationSavedConfirmationTest {

    @Mock private Callback<BailCase> callback;

    @Mock private CaseDetails<BailCase> caseDetails;

    private BailApplicationSavedConfirmation bailApplicationSavedConfirmation;

    @BeforeEach
    public void setUp() {
        bailApplicationSavedConfirmation = new BailApplicationSavedConfirmation();
        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
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
