package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;


@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UploadHomeOfficeAppealResponseConfirmationTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private UserDetails userDetails;
    @Mock private UserDetailsHelper userDetailsHelper;

    private UploadHomeOfficeAppealResponseConfirmation confirmation;

    @BeforeEach
    void setUp() {
        confirmation = new UploadHomeOfficeAppealResponseConfirmation(userDetails, userDetailsHelper);
        when(callback.getEvent()).thenReturn(Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE);
    }

    private void stubCaseData() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_return_home_office_confirmation_for_non_24_week_case() {
        stubCaseData();
        when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.empty());

        PostSubmitCallbackResponse callbackResponse = confirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(callbackResponse.getConfirmationHeader().get())
            .contains("You've uploaded the appeal response");

        assertThat(callbackResponse.getConfirmationBody().get())
            .contains("Do this next");

        assertThat(callbackResponse.getConfirmationBody().get())
            .contains(
                "The Tribunal will: \n* check that the Home Office response complies with the Procedure Rules and Practice Directions\n* inform you of any issues\n\nProviding there are no issues, the response will be shared with the appellant.");
    }

    @Test
    void should_return_home_office_confirmation_for_24_week_case() {
        stubCaseData();
        when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.HOME_OFFICE_GENERIC);

        PostSubmitCallbackResponse callbackResponse = confirmation.handle(callback);

        assertThat(callbackResponse.getConfirmationBody().get())
            .contains("Do this next");

        assertThat(callbackResponse.getConfirmationBody().get())
            .contains(
                "The Tribunal will: \n* check that the Home Office response complies with the Procedure Rules and Practice Directions\n* inform you of any issues\n\nProviding there are no issues, the response will be shared with the appellant.");
    }

    @Test
    void should_return_legal_officer_confirmation_for_24_week_case() {
        stubCaseData();
        when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.TRIBUNAL_CASEWORKER);

        PostSubmitCallbackResponse callbackResponse = confirmation.handle(callback);

        assertThat(callbackResponse.getConfirmationBody().get())
            .contains("Do this next");

        assertThat(callbackResponse.getConfirmationBody().get())
            .contains("Check the response uploaded by the respondent.");

        assertThat(callbackResponse.getConfirmationBody().get())
            .contains("Force case - Prepare for hearing");

        assertThat(callbackResponse.getConfirmationBody().get())
            .contains("If it does not comply direct the respondent to make the appropriate changes.");
    }

    @Test
    void should_return_admin_officer_confirmation_for_24_week_case() {
        stubCaseData();
        when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.ADMIN_OFFICER);

        PostSubmitCallbackResponse callbackResponse = confirmation.handle(callback);

        assertThat(callbackResponse.getConfirmationBody().get())
            .contains("Check the response uploaded by the respondent.");
    }

    @Test
    void should_return_legal_rep_confirmation_for_24_week_case() {
        stubCaseData();
        when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.LEGAL_REPRESENTATIVE);

        PostSubmitCallbackResponse callbackResponse = confirmation.handle(callback);

        assertThat(callbackResponse.getConfirmationBody().get())
            .contains("Do this next");

        assertThat(callbackResponse.getConfirmationBody().get())
            .contains("The case has now been sent to the respondent for review.");

        assertThat(callbackResponse.getConfirmationBody().get())
            .contains("You'll get an email letting you know when it's there.");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        reset(callback);
        assertThatThrownBy(() -> confirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = confirmation.canHandle(callback);

            if (event == Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> confirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
