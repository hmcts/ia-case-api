package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
public class MoveApplicationToDecidedConfirmationTest {

    @Mock private Callback<BailCase> callback;

    @Mock private CaseDetails<BailCase> caseDetails;
    private MoveApplicationToDecidedConfirmation moveApplicationToDecidedConfirmation;

    @BeforeEach
    public void setUp() {
        moveApplicationToDecidedConfirmation = new MoveApplicationToDecidedConfirmation();
        when(callback.getEvent()).thenReturn(Event.MOVE_APPLICATION_TO_DECIDED);
    }

    @Test
    void should_not_allow_null_args() {
        assertThatThrownBy(() -> moveApplicationToDecidedConfirmation.handle(null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_handle_invalid_callback() {
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION); //Invalid event for this handler

        assertThatThrownBy(() -> moveApplicationToDecidedConfirmation.handle(callback)).isExactlyInstanceOf(
            IllegalStateException.class);
    }
}
