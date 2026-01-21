package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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

@MockitoSettings(strictness = Strictness.LENIENT)
public class ShowPreviousApplicationConfirmationTest {

    @Mock private Callback<BailCase> callback;

    @Mock private CaseDetails<BailCase> caseDetails;
    private ShowPreviousApplicationConfirmation showPreviousApplicationConfirmation;

    @BeforeEach
    public void setUp() {
        showPreviousApplicationConfirmation = new ShowPreviousApplicationConfirmation();
        when(callback.getEvent()).thenReturn(Event.VIEW_PREVIOUS_APPLICATIONS);
    }

    @Test
    void should_not_allow_null_args() {
        assertThatThrownBy(() -> showPreviousApplicationConfirmation.handle(null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_handle_invalid_callback() {
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION); //Invalid event for this handler

        assertThatThrownBy(() -> showPreviousApplicationConfirmation.handle(callback)).isExactlyInstanceOf(
            IllegalStateException.class);
    }
}
