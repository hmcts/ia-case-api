package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECIDE_AN_APPLICATION_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REASON_FOR_LINK_APPEAL;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ReasonForLinkAppealOptions;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DecideAnApplicationConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Mock
    private DateProvider dateProvider;

    @InjectMocks
    private DecideAnApplicationConfirmation decideAnApplicationConfirmation =
        new DecideAnApplicationConfirmation();

    @BeforeEach
    public void setUp() {

        MockitoAnnotations.openMocks(this);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.DECIDE_AN_APPLICATION);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Adjourn",
        "Expedite",
        "Transfer",
        "Link/unlink appeals",
        "Judge's review of application decision",
        "Reinstate an ended appeal",
        "Time extension",
        "Update appeal details",
        "Update hearing requirements",
        "Withdraw"
    })
    void should_return_valid_confirmation_message_for_granted(String type) {

        when(dateProvider.now()).thenReturn(LocalDate.MAX);
        List<IdValue<Document>> evidence = List.of(new IdValue<>("1",
            new Document("http://localhost/documents/123456",
                "http://localhost/documents/123456",
                "DocumentName.pdf"
            )
        ));
        MakeAnApplication makeAnApplication =
            new MakeAnApplication("Legal representative", type, "A reason to update appeal details",
                evidence, dateProvider.now().toString(), "Pending",
                State.LISTING.toString());
        makeAnApplication.setApplicantRole("caseworker-ia-caseofficer");
        makeAnApplication.setDecision("Granted");
        final List<IdValue<MakeAnApplication>> makeAnApplications =
            List.of(new IdValue<>("1", makeAnApplication));

        when(asylumCase.read(DECIDE_AN_APPLICATION_ID, String.class)).thenReturn(Optional.of("1"));
        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));
        when(asylumCase.read(REASON_FOR_LINK_APPEAL, ReasonForLinkAppealOptions.class))
            .thenReturn(Optional.of(ReasonForLinkAppealOptions.BAIL));

        PostSubmitCallbackResponse callbackResponse = decideAnApplicationConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getConfirmationHeader()).contains("# You have decided an application");

        long id = callback.getCaseDetails().getId();
        String expectedBody = switch (type) {
            case "Adjourn" -> "#### What happens next\n\n"
                    + "The application decision has been recorded and is now available in the applications tab. "
                    + "You must now [record the details of the adjournment]"
                    + "(/case/IA/Asylum/" + id + "/trigger/recordAdjournmentDetails).";

            case "Expedite", "Transfer" -> "#### What happens next\n\n"
                    + "The application decision has been recorded and is now available in the applications tab. "
                    + "You must now [update the hearing request]"
                    + "(/case/IA/Asylum/" + id + "/trigger/updateHearingRequest).";

            case "Link/unlink appeals" -> "#### What happens next\n\n"
                    + "The application decision has been recorded and is now available in the applications tab. "
                    + "You must now [link the appeal](/case/IA/Asylum/"
                    + id + "/trigger/linkAppeal)"
                    + " or [unlink the appeal](/case/IA/Asylum/"
                    + id + "/trigger/unlinkAppeal).";

            case "Judge's review of application decision" -> "#### What happens next\n\n"
                    + "The application decision has been recorded and is now available in the applications tab. "
                    + "Both parties will receive a notification detailing your decision.";

            case "Reinstate an ended appeal" -> "#### What happens next\n\n"
                    + "The application decision has been recorded and is now available in the applications tab. "
                    + "You now need to [reinstate the appeal](/case/IA/Asylum/" + id + "/trigger/reinstateAppeal)";

            case "Time extension" -> "#### What happens next\n\n"
                    + "The application decision has been recorded and is now available in the applications tab. "
                    + "You must now [change the direction's due date](/case/IA/Asylum/"
                    + id + "/trigger/changeDirectionDueDate)";

            case "Update appeal details" -> "#### What happens next\n\n"
                    + "The application decision has been recorded and is now available in the applications tab. "
                    + "You must now [update the appeal details](/case/IA/Asylum/"
                    + id + "/trigger/editAppealAfterSubmit)";

            case "Update hearing requirements" -> "#### What happens next\n\n"
                    + "The application decision has been recorded and is now available in the applications tab. "
                    + "You must now [update the hearing requirements](/case/IA/Asylum/"
                    + id + "/trigger/updateHearingRequirements)";

            case "Withdraw" -> "#### What happens next\n\n"
                    + "The application decision has been recorded and is now available in the applications tab. "
                    + "You must now [end the appeal](/case/IA/Asylum/" + id + "/trigger/endAppeal)";

            default -> "#### What happens next\n\n"
                    + "The application decision has been recorded and is now available in the applications tab. ";
        };
        assertThat(callbackResponse.getConfirmationBody()).contains(expectedBody);
    }

    @Test
    void should_return_valid_confirmation_message_for_refused() {

        when(dateProvider.now()).thenReturn(LocalDate.MAX);
        List<IdValue<Document>> evidence =
            Arrays.asList(new IdValue<>("1",
                new Document("http://localhost/documents/123456",
                    "http://localhost/documents/123456",
                    "DocumentName.pdf")));
        MakeAnApplication makeAnApplication =
            new MakeAnApplication("Legal representative", "Adjourn", "A reason to update appeal details",
                evidence, dateProvider.now().toString(), "Pending",
                State.LISTING.toString());
        makeAnApplication.setApplicantRole("caseworker-ia-caseofficer");
        makeAnApplication.setDecision("Refused");
        final List<IdValue<MakeAnApplication>> makeAnApplications =
            Arrays.asList(new IdValue<>("1", makeAnApplication));

        when(asylumCase.read(DECIDE_AN_APPLICATION_ID, String.class)).thenReturn(Optional.of("1"));
        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));

        PostSubmitCallbackResponse callbackResponse = decideAnApplicationConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You have decided an application");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "#### What happens next\n\n"
                    + "The application decision has been recorded and is now available in the applications tab. "
                    + "Both parties will be notified that the application was refused."
            );
    }

    @Test
    void should_throw_on_missing_application_id() {

        Assertions.assertThatThrownBy(() -> decideAnApplicationConfirmation.handle(callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Application id is not present");
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = decideAnApplicationConfirmation.canHandle(callback);

            if (event == Event.DECIDE_AN_APPLICATION) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> decideAnApplicationConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideAnApplicationConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
