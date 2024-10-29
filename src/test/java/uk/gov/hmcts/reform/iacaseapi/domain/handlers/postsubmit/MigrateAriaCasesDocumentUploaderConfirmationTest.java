package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class MigrateAriaCasesDocumentUploaderConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private MigrateAriaCasesDocumentUploaderConfirmation migrateAriaCasesDocumentUploaderConfirmation
        = new MigrateAriaCasesDocumentUploaderConfirmation();

    @BeforeEach
    void setUp() {
        when(callback.getEvent()).thenReturn(Event.PROGRESS_MIGRATED_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.ARIA_DESIRED_STATE, State.class)).thenReturn(Optional.of(State.APPEAL_SUBMITTED));
    }

    @Test
    void should_return_confirmation() {
        PostSubmitCallbackResponse callbackResponse =
            migrateAriaCasesDocumentUploaderConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains(String.format("# You have progressed this case \n## New state: \n## '%s'", "Appeal submitted"));

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### What happens next\n\n"
                + "You can add or edit documents at any time through the 'Next step' \n"
                + "dropdown list in your case details, using 'Edit documents'.\n\n"
                + "#### If this case was listed for a hearing\n\n"
                + "Listings have not been migrated. If this case was listed for a hearing,     \n"
                + "you must transfer the listing over to the hearing management     \n"
                + "component (HMC) as part of the migration process.");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);

        assertThatThrownBy(() -> migrateAriaCasesDocumentUploaderConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = migrateAriaCasesDocumentUploaderConfirmation.canHandle(callback);

            if (event == Event.PROGRESS_MIGRATED_CASE) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> migrateAriaCasesDocumentUploaderConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> migrateAriaCasesDocumentUploaderConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_throw_if_aria_desired_not_present() {
        when(asylumCase.read(AsylumCaseFieldDefinition.ARIA_DESIRED_STATE, State.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> migrateAriaCasesDocumentUploaderConfirmation.handle(callback))
            .hasMessage("ariaDesiredState is not present")
            .isExactlyInstanceOf(IllegalStateException.class);

    }
}
