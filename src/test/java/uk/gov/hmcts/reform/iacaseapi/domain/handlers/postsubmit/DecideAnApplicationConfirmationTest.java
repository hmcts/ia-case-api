package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

@RunWith(JUnitParamsRunner.class)
@SuppressWarnings("unchecked")
public class DecideAnApplicationConfirmationTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Mock private DateProvider dateProvider;

    @InjectMocks
    private DecideAnApplicationConfirmation decideAnApplicationConfirmation =
        new DecideAnApplicationConfirmation();

    @Before
    public void setUp() {

        MockitoAnnotations.openMocks(this);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.DECIDE_AN_APPLICATION);
    }

    @Test
    @Parameters({
        "Adjourn", "Expedite", "Transfer",
        "Link/unlink appeals",
        "Judge's review of application decision",
        "Reinstate an ended appeal",
        "Time extension",
        "Update appeal details",
        "Update hearing requirements",
        "Withdraw"
    })
    public void should_return_valid_confirmation_message_for_granted(String type) {

        when(dateProvider.now()).thenReturn(LocalDate.MAX);
        List<IdValue<Document>> evidence =
            Arrays.asList(new IdValue<>("1",
                new Document("http://localhost/documents/123456",
                    "http://localhost/documents/123456",
                    "DocumentName.pdf")));
        MakeAnApplication makeAnApplication =
            new MakeAnApplication("Legal representative", type, "A reason to update appeal details",
                evidence, dateProvider.now().toString(), "Pending",
                State.LISTING.toString());
        makeAnApplication.setApplicantRole("caseworker-ia-caseofficer");
        makeAnApplication.setDecision("Granted");
        final List<IdValue<MakeAnApplication>> makeAnApplications = Arrays.asList(new IdValue<>("1", makeAnApplication));

        when(asylumCase.read(DECIDE_AN_APPLICATION_ID, String.class)).thenReturn(Optional.of("1"));
        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));
        when(asylumCase.read(REASON_FOR_LINK_APPEAL, ReasonForLinkAppealOptions.class))
            .thenReturn(Optional.of(ReasonForLinkAppealOptions.BAIL));

        PostSubmitCallbackResponse callbackResponse = decideAnApplicationConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("You have decided an application")
        );

        switch (type) {
            case "Adjourn":
            case "Expedite":
            case "Transfer":
                assertThat(
                    callbackResponse.getConfirmationBody().get(),
                    containsString(
                        "#### What happens next\n\n"
                        + "The application decision has been recorded and is now available in the applications tab. "
                        + "You need to tell the listing team to relist the case. Once the case is relisted a new Notice of Hearing "
                        + "will be sent to all parties.")
                );
                break;

            case "Link/unlink appeals":
                assertThat(
                    callbackResponse.getConfirmationBody().get(),
                    containsString(
                        "#### What happens next\n\n"
                        + "The application decision has been recorded and is now available in the applications tab. "
                        + "You must now [link the appeal](/case/IA/Asylum/"
                        + callback.getCaseDetails().getId() + "/trigger/linkAppeal)"
                        + " or [unlink the appeal](/case/IA/Asylum/"
                        + callback.getCaseDetails().getId() + "/trigger/unlinkAppeal).")
                );
                break;

            case "Judge's review of application decision":
                assertThat(
                    callbackResponse.getConfirmationBody().get(),
                    containsString(
                        "#### What happens next\n\n"
                        + "The application decision has been recorded and is now available in the applications tab. "
                        + "Both parties will receive a notification detailing your decision"
                    )
                );
                break;

            case "Reinstate an ended appeal":
                assertThat(
                    callbackResponse.getConfirmationBody().get(),
                    containsString(
                        "#### What happens next\n\n"
                        + "The application decision has been recorded and is now available in the applications tab. "
                        + "You now need to [reinstate the appeal](/case/IA/Asylum/"
                        + callback.getCaseDetails().getId() + "/trigger/reinstateAppeal)"
                    )
                );
                break;

            case "Time extension":
                assertThat(
                    callbackResponse.getConfirmationBody().get(),
                    containsString(
                        "#### What happens next\n\n"
                        + "The application decision has been recorded and is now available in the applications tab. "
                        + "You must now [change the direction's due date](/case/IA/Asylum/"
                        + callback.getCaseDetails().getId() + "/trigger/changeDirectionDueDate)"
                    )
                );
                break;

            case "Update appeal details":
                assertThat(
                    callbackResponse.getConfirmationBody().get(),
                    containsString(
                        "#### What happens next\n\n"
                        + "The application decision has been recorded and is now available in the applications tab. "
                        + "You must now [update the appeal details](/case/IA/Asylum/"
                        + callback.getCaseDetails().getId() + "/trigger/editAppealAfterSubmit)"
                    )
                );
                break;

            case "Update hearing requirements":
                assertThat(
                    callbackResponse.getConfirmationBody().get(),
                    containsString(
                        "#### What happens next\n\n"
                        + "The application decision has been recorded and is now available in the applications tab. "
                        + "You must now [update the hearing requirements](/case/IA/Asylum/"
                        + callback.getCaseDetails().getId() + "/trigger/updateHearingRequirements)"
                    )
                );
                break;

            case "Withdraw":
                assertThat(
                    callbackResponse.getConfirmationBody().get(),
                    containsString(
                        "#### What happens next\n\n"
                        + "The application decision has been recorded and is now available in the applications tab. "
                        + "You must now [end the appeal](/case/IA/Asylum/"
                        + callback.getCaseDetails().getId() + "/trigger/endAppeal)"
                    )
                );
                break;

            default:
                assertThat(
                    callbackResponse.getConfirmationBody().get(),
                    containsString(
                        "#### What happens next\n\n"
                        + "The application decision has been recorded and is now available in the applications tab. "
                    )
                );

        }
    }

    @Test
    public void should_return_valid_confirmation_message_for_refused() {

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
        final List<IdValue<MakeAnApplication>> makeAnApplications = Arrays.asList(new IdValue<>("1", makeAnApplication));

        when(asylumCase.read(DECIDE_AN_APPLICATION_ID, String.class)).thenReturn(Optional.of("1"));
        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));

        PostSubmitCallbackResponse callbackResponse = decideAnApplicationConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertThat(
            callbackResponse.getConfirmationHeader().get(),
            containsString("You have decided an application")
        );
        assertThat(
            callbackResponse.getConfirmationBody().get(),
            containsString(
                "#### What happens next\n\n"
                + "The application decision has been recorded and is now available in the applications tab. "
                + "Both parties will be notified that the application was refused."
            )
        );
    }

    @Test
    public void should_throw_on_missing_application_id() {

        Assertions.assertThatThrownBy(() -> decideAnApplicationConfirmation.handle(callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Application id is not present");
    }

    @Test
    public void it_can_handle_callback() {

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
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> decideAnApplicationConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideAnApplicationConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
