package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdSupplementaryUpdater;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CreateFlagConfirmationTest {

    @Mock
    private CcdSupplementaryUpdater ccdSupplementaryUpdater;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CreateFlagConfirmation createFlagConfirmation =
            new CreateFlagConfirmation(ccdSupplementaryUpdater);

    @BeforeEach
    void setUp() {
        createFlagConfirmation = new CreateFlagConfirmation(ccdSupplementaryUpdater);
    }

    @Test
    void should_invoke_supplementary_updater() {
        when(callback.getEvent()).thenReturn(Event.CREATE_FLAG);

        createFlagConfirmation.handle(callback);

        verify(ccdSupplementaryUpdater).setAppellantLevelFlagsSupplementary(callback);
    }

    @Test
    void should_return_confirmation() {

        when(callback.getEvent()).thenReturn(Event.CREATE_FLAG);

        PostSubmitCallbackResponse callbackResponse =
            createFlagConfirmation.handle(callback);

        assertNotNull(callbackResponse);

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> createFlagConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = createFlagConfirmation.canHandle(callback);

            if (event == Event.CREATE_FLAG) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> createFlagConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }
}
