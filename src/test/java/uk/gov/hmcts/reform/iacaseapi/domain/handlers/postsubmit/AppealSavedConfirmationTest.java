package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ref.OrganisationEntityResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdCaseAssignment;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ProfessionalOrganisationRetriever;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.Organisation;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.OrganisationPolicy;


@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AppealSavedConfirmationTest {

    @Mock ProfessionalOrganisationRetriever professionalOrganisationRetriever;
    @Mock OrganisationEntityResponse organisationEntityResponse;
    @Mock CcdCaseAssignment ccdCaseAssignment;
    @Mock private FeatureToggler featureToggler;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private AppealSavedConfirmation appealSavedConfirmation;
    private final String organisationIdentifier = "ZE2KIWO";
    private OrganisationPolicy organisationPolicy;


    @BeforeEach
    public void setUp() throws Exception {

        appealSavedConfirmation = new AppealSavedConfirmation(
            professionalOrganisationRetriever,
            ccdCaseAssignment,
            featureToggler
        );

        organisationPolicy =
            OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                    .organisationID("somOrgId")
                    .build()
                )
                .orgPolicyCaseAssignedRole("[LEGALREPRESENTATIVE]")
                .build();

        when(asylumCase.read(LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class))
            .thenReturn(Optional.of(organisationPolicy));
    }

    @Test
    void should_return_confirmation() {

        long caseId = 1234;
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.DC));
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);

        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);
        when(featureToggler.getValue("share-case-feature", false)).thenReturn(true);

        PostSubmitCallbackResponse callbackResponse =
            appealSavedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have saved your appeal");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("### Do this next");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "If you're ready to submit your appeal, select 'Submit your appeal' in " +
                    "the 'Next step' dropdown list from your case details page."
            );

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("Not ready to submit your appeal yet?");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("You can return to the case details page to make changes from the ‘Next step’ dropdown list.");

        verify(ccdCaseAssignment, times(1)).revokeAccessToCase(callback, organisationIdentifier);
    }

    @Test
    void should_return_confirmation_for_pay_EA() {

        long caseId = 1234;

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);

        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);
        when(featureToggler.getValue("share-case-feature", false)).thenReturn(true);

        PostSubmitCallbackResponse callbackResponse =
            appealSavedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have saved your appeal");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("### Do this next");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                ""
            );

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("Not ready to submit your appeal yet?");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("You can return to the case details page to make changes from the ‘Next step’ dropdown list.");

        verify(ccdCaseAssignment, times(1)).revokeAccessToCase(callback, organisationIdentifier);
    }

    @Test
    void should_return_confirmation_for_EA_with_Remission() {

        long caseId = 1234;

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION));
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);

        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);
        when(featureToggler.getValue("share-case-feature", false)).thenReturn(true);

        PostSubmitCallbackResponse callbackResponse =
            appealSavedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have saved your appeal");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("### Do this next");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                ""
            );

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("Not ready to submit your appeal yet?");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("You can return to the case details page to make changes from the ‘Next step’ dropdown list.");

        verify(ccdCaseAssignment, times(1)).revokeAccessToCase(callback, organisationIdentifier);
    }

    @Test
    void should_return_confirmation_for_pay_hu() {

        long caseId = 1234;

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));

        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);
        when(featureToggler.getValue("share-case-feature", false)).thenReturn(true);

        PostSubmitCallbackResponse callbackResponse =
            appealSavedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have saved your appeal");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("### Do this next");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                ""
            );

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("Not ready to submit your appeal yet?");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("You can return to the case details page to make changes from the ‘Next step’ dropdown list.");

        verify(ccdCaseAssignment, times(1)).revokeAccessToCase(callback, organisationIdentifier);
    }

    @Test
    void should_return_confirmation_for_pay_now_pa() {

        long caseId = 1234;

        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));

        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);
        when(featureToggler.getValue("share-case-feature", false)).thenReturn(true);


        PostSubmitCallbackResponse callbackResponse =
            appealSavedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have saved your appeal");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("### Do this next");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                ""
            );

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("Not ready to submit your appeal yet?");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("You can return to the case details page to make changes from the ‘Next step’ dropdown list.");

        verify(ccdCaseAssignment, times(1)).revokeAccessToCase(callback, organisationIdentifier);
    }

    @Test
    void should_return_confirmation_for_submit() {

        long caseId = 1234;

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));

        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);
        when(featureToggler.getValue("share-case-feature", false)).thenReturn(true);


        PostSubmitCallbackResponse callbackResponse =
            appealSavedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have saved your appeal");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("### Do this next");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                ""
            );

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("Not ready to submit your appeal yet?");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("You can return to the case details page to make changes from the ‘Next step’ dropdown list.");

        verify(ccdCaseAssignment, times(1)).revokeAccessToCase(callback, organisationIdentifier);
    }

    @Test
    void should_return_confirmation_for_PA_pay() {

        long caseId = 1234;

        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));

        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);
        when(featureToggler.getValue("share-case-feature", false)).thenReturn(true);


        PostSubmitCallbackResponse callbackResponse =
            appealSavedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                ""
            );

        verify(ccdCaseAssignment, times(1)).revokeAccessToCase(callback, organisationIdentifier);
    }

    @Test
    void should_return_confirmation_for_PA_submit() {

        long caseId = 1234;

        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));

        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);
        when(featureToggler.getValue("share-case-feature", false)).thenReturn(true);

        PostSubmitCallbackResponse callbackResponse =
            appealSavedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                ""
            );

        verify(ccdCaseAssignment, times(1)).revokeAccessToCase(callback, organisationIdentifier);
    }

    @Test
    void should_return_confirmation_for_EA_submit() {

        long caseId = 1234;

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);
        when(featureToggler.getValue("share-case-feature", false)).thenReturn(true);

        PostSubmitCallbackResponse callbackResponse =
            appealSavedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                ""
            );

        verify(ccdCaseAssignment, times(1)).revokeAccessToCase(callback, organisationIdentifier);
    }

    @Test
    void should_not_assign_or_revoke_access_to_case_for_edit_appeal() {

        long caseId = 1234;

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));

        PostSubmitCallbackResponse callbackResponse =
            appealSavedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        verify(ccdCaseAssignment, times(0)).revokeAccessToCase(callback, organisationIdentifier);
    }

    @Test
    void should_not_assign_or_revoke_access_to_case_when_local_authority_policy_is_not_present() {

        long caseId = 1234;

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class)).thenReturn(Optional.empty());

        PostSubmitCallbackResponse callbackResponse =
            appealSavedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        verify(ccdCaseAssignment, times(0)).revokeAccessToCase(callback, organisationIdentifier);
    }

    @Test
    void should_not_assign_or_revoke_access_to_case_when_feature_flag_disabled() {

        long caseId = 1234;

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(featureToggler.getValue("share-case-feature", false)).thenReturn(false);

        PostSubmitCallbackResponse callbackResponse =
            appealSavedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        verify(ccdCaseAssignment, times(0)).revokeAccessToCase(callback, organisationIdentifier);
    }

    @Test
    void should_return_confirmation_for_internal_cases_admin() {

        long caseId = 1234;

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);
        when(featureToggler.getValue("share-case-feature", false)).thenReturn(true);

        PostSubmitCallbackResponse callbackResponse =
            appealSavedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("# You have saved your appeal");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("### Do this next");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "If you're ready to submit your appeal, select 'Submit your appeal' in " +
                    "the 'Next step' dropdown list from your case details page."
            );

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("Not ready to submit your appeal yet?");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("You can return to the case details page to make changes from the ‘Next step’ dropdown list.");

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));

        assertThatThrownBy(() -> appealSavedConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getEvent()).thenReturn(event);
            when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));

            boolean canHandle = appealSavedConfirmation.canHandle(callback);

            if (event == Event.START_APPEAL || event == Event.EDIT_APPEAL) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void it_can_not_handle_callback_for_aip_journey() {

        for (Event event : Event.values()) {

            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getEvent()).thenReturn(event);
            when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

            boolean canHandle = appealSavedConfirmation.canHandle(callback);

            if (event == Event.START_APPEAL || event == Event.EDIT_APPEAL) {

                assertFalse(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> appealSavedConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSavedConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
