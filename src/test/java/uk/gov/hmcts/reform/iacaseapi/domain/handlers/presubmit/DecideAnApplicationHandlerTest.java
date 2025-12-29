package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECIDE_AN_APPLICATION_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_APPLICATIONS_TO_DECIDE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_INTEGRATED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LAST_MODIFIED_APPLICATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATIONS_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATION_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATION_DECISION_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATION_FIELDS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationDecision.GRANTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationDecision.REFUSED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.ENDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.FINAL_BUNDLING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.LISTING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.PREPARE_FOR_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.PRE_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.SUBMIT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.client.RestClientException;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ServiceResponseException;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DecideAnApplicationHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private AsylumCase updatedAsylumCase;

    @Mock private DateProvider dateProvider;
    @Mock private UserDetails userDetails;
    @Mock private UserDetailsHelper userDetailsHelper;
    @Mock private FeatureToggler featureToggler;
    @Mock private IaHearingsApiService iaHearingsApiService;
    private static final Set<State> STATES_FOR_HEARING_CANCELLATION = Set.of(
        LISTING,
        PREPARE_FOR_HEARING,
        FINAL_BUNDLING,
        PRE_HEARING,
        DECISION
    );
    private DynamicList makeAnApplicationsList;
    private List<IdValue<Document>> evidence;
    private MakeAnApplication makeAnApplication;

    private DecideAnApplicationHandler decideAnApplicationHandler;

    @BeforeEach
    public void setUp() {

        decideAnApplicationHandler = new DecideAnApplicationHandler(
            dateProvider, userDetails, userDetailsHelper, featureToggler, iaHearingsApiService
        );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.DECIDE_AN_APPLICATION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        makeAnApplicationsList = new DynamicList(
            new Value("1", "Legal representative : Application 1"),
            Arrays.asList(new Value("1", "Legal representative : Application 1")));
        evidence = Arrays.asList(new IdValue<>("1",
            new Document("http://localhost/documents/123456",
                "http://localhost/documents/123456",
                "DocumentName.pdf")));
        when(dateProvider.now()).thenReturn(LocalDate.MAX);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void should_handle_the_about_to_submit(boolean waR2FeatureFlag) {

        makeAnApplication =
            new MakeAnApplication("Legal representative", "Update appeal details", "A reason to update appeal details",
                evidence, dateProvider.now().toString(), "Pending",
                State.LISTING.toString());
        makeAnApplication.setApplicantRole("caseworker-ia-legalrep-solicitor");

        List<IdValue<MakeAnApplication>> makeAnApplications = List.of(new IdValue<>("1", makeAnApplication));

        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));

        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.TRIBUNAL_CASEWORKER);

        when(asylumCase.read(MAKE_AN_APPLICATIONS_LIST, DynamicList.class))
            .thenReturn(Optional.of(makeAnApplicationsList));
        when(asylumCase.read(MAKE_AN_APPLICATION_DECISION, MakeAnApplicationDecision.class))
            .thenReturn(Optional.of(GRANTED));
        when(asylumCase.read(MAKE_AN_APPLICATION_DECISION_REASON, String.class))
            .thenReturn(Optional.of("A reason of the decision"));

        if (waR2FeatureFlag) {
            when(featureToggler.getValue("wa-R2-feature", false)).thenReturn(true);
        }

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decideAnApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(DECIDE_AN_APPLICATION_ID, "1");
        verify(asylumCase, times(1)).write(MAKE_AN_APPLICATIONS, asylumCase.read(MAKE_AN_APPLICATIONS));
        verify(asylumCase, times(1)).write(HAS_APPLICATIONS_TO_DECIDE, NO);

        if (waR2FeatureFlag) {
            verify(asylumCase, times(1)).write(LAST_MODIFIED_APPLICATION, makeAnApplication);
        } else {
            verify(asylumCase, times(0)).write(LAST_MODIFIED_APPLICATION, makeAnApplication);
        }

        verify(iaHearingsApiService, never()).aboutToSubmit(callback);
        verify(asylumCase, times(1)).clear(MAKE_AN_APPLICATIONS_LIST);
        verify(asylumCase, times(1)).clear(MAKE_AN_APPLICATION_FIELDS);
        verify(asylumCase, times(1)).clear(MAKE_AN_APPLICATION_DECISION);
        verify(asylumCase, times(1)).clear(MAKE_AN_APPLICATION_DECISION_REASON);
    }

    static Stream<Arguments> makeAnApplicationParameters() {
        return Stream.of(
            Arguments.of(GRANTED, LISTING, YES),
            Arguments.of(GRANTED, PREPARE_FOR_HEARING, YES),
            Arguments.of(GRANTED, FINAL_BUNDLING, YES),
            Arguments.of(GRANTED, PRE_HEARING, YES),
            Arguments.of(GRANTED, DECISION, YES),
            Arguments.of(REFUSED, LISTING, YES),
            Arguments.of(REFUSED, PREPARE_FOR_HEARING, YES),
            Arguments.of(REFUSED, FINAL_BUNDLING, YES),
            Arguments.of(REFUSED, PRE_HEARING, YES),
            Arguments.of(REFUSED, DECISION, YES),
            Arguments.of(GRANTED, ENDED, YES),
            Arguments.of(REFUSED, SUBMIT_HEARING_REQUIREMENTS, YES),
            Arguments.of(GRANTED, LISTING, NO),
            Arguments.of(GRANTED, PREPARE_FOR_HEARING, NO),
            Arguments.of(GRANTED, FINAL_BUNDLING, NO),
            Arguments.of(GRANTED, PRE_HEARING, NO),
            Arguments.of(GRANTED, DECISION, NO),
            Arguments.of(REFUSED, LISTING, NO),
            Arguments.of(REFUSED, PREPARE_FOR_HEARING, NO),
            Arguments.of(REFUSED, FINAL_BUNDLING, NO),
            Arguments.of(REFUSED, PRE_HEARING, NO),
            Arguments.of(REFUSED, DECISION, NO),
            Arguments.of(GRANTED, ENDED, NO),
            Arguments.of(REFUSED, SUBMIT_HEARING_REQUIREMENTS, NO)
        );
    }

    @ParameterizedTest
    @MethodSource("makeAnApplicationParameters")
    void should_send_hearing_cancellation_request_when_appropriate(MakeAnApplicationDecision decision,
                                                                   State state,
                                                                   YesOrNo isIntegrated) {

        makeAnApplication =
            new MakeAnApplication("Legal representative", "Change decision type (ie with or without a hearing)", "A reason to change decision type",
                evidence, dateProvider.now().toString(), "Pending",
                State.LISTING.toString());
        makeAnApplication.setApplicantRole("caseworker-ia-legalrep-solicitor");

        List<IdValue<MakeAnApplication>> makeAnApplications = List.of(new IdValue<>("1", makeAnApplication));

        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));

        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.TRIBUNAL_CASEWORKER);

        when(asylumCase.read(MAKE_AN_APPLICATIONS_LIST, DynamicList.class))
            .thenReturn(Optional.of(makeAnApplicationsList));
        when(asylumCase.read(MAKE_AN_APPLICATION_DECISION, MakeAnApplicationDecision.class))
            .thenReturn(Optional.of(decision));
        when(asylumCase.read(MAKE_AN_APPLICATION_DECISION_REASON, String.class))
            .thenReturn(Optional.of("A reason of the decision"));
        when(featureToggler.getValue("wa-R2-feature", false)).thenReturn(true);
        when(iaHearingsApiService.aboutToSubmit(callback)).thenReturn(updatedAsylumCase);
        when(updatedAsylumCase.read(MANUAL_CANCEL_HEARINGS_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(caseDetails.getState()).thenReturn(state);
        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(isIntegrated));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decideAnApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        if (decision == GRANTED
            && STATES_FOR_HEARING_CANCELLATION.contains(state)
            && isIntegrated == YES) {
            verify(iaHearingsApiService, times(1)).aboutToSubmit(callback);
        } else {
            verify(iaHearingsApiService, never()).aboutToSubmit(callback);
        }
    }

    /*
    Delegation to ia-hearings-api successful, but call between ia-hearings-api and HMC unsuccessful
     */
    @Test
    void should_add_error_if_cancellation_call_unsuccessful_in_ia_hearings_api() {

        makeAnApplication =
            new MakeAnApplication("Legal representative", "Change decision type (ie with or without a hearing)", "A reason to change decision type",
                evidence, dateProvider.now().toString(), "Pending",
                State.LISTING.toString());
        makeAnApplication.setApplicantRole("caseworker-ia-legalrep-solicitor");

        List<IdValue<MakeAnApplication>> makeAnApplications = List.of(new IdValue<>("1", makeAnApplication));

        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));

        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.TRIBUNAL_CASEWORKER);

        when(asylumCase.read(MAKE_AN_APPLICATIONS_LIST, DynamicList.class))
            .thenReturn(Optional.of(makeAnApplicationsList));
        when(asylumCase.read(MAKE_AN_APPLICATION_DECISION, MakeAnApplicationDecision.class))
            .thenReturn(Optional.of(GRANTED));
        when(asylumCase.read(MAKE_AN_APPLICATION_DECISION_REASON, String.class))
            .thenReturn(Optional.of("A reason of the decision"));
        when(featureToggler.getValue("wa-R2-feature", false)).thenReturn(true);
        when(iaHearingsApiService.aboutToSubmit(callback)).thenReturn(updatedAsylumCase);
        when(updatedAsylumCase.read(MANUAL_CANCEL_HEARINGS_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(caseDetails.getState()).thenReturn(LISTING);
        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decideAnApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(1, callbackResponse.getErrors().size());
        assertNotNull(callbackResponse);
    }

    /*
    Delegation to ia-hearings-api unsuccessful
    */
    @Test
    void should_add_error_if_delegation_to_ia_hearings_api_unsuccessful() {

        makeAnApplication =
            new MakeAnApplication("Legal representative", "Change decision type (ie with or without a hearing)", "A reason to change decision type",
                evidence, dateProvider.now().toString(), "Pending",
                State.LISTING.toString());
        makeAnApplication.setApplicantRole("caseworker-ia-legalrep-solicitor");

        List<IdValue<MakeAnApplication>> makeAnApplications = List.of(new IdValue<>("1", makeAnApplication));

        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));

        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.TRIBUNAL_CASEWORKER);

        when(asylumCase.read(MAKE_AN_APPLICATIONS_LIST, DynamicList.class))
            .thenReturn(Optional.of(makeAnApplicationsList));
        when(asylumCase.read(MAKE_AN_APPLICATION_DECISION, MakeAnApplicationDecision.class))
            .thenReturn(Optional.of(GRANTED));
        when(asylumCase.read(MAKE_AN_APPLICATION_DECISION_REASON, String.class))
            .thenReturn(Optional.of("A reason of the decision"));
        when(featureToggler.getValue("wa-R2-feature", false)).thenReturn(true);
        when(iaHearingsApiService.aboutToSubmit(callback))
            .thenThrow(new ServiceResponseException("Error", new RestClientException("Error")));
        when(caseDetails.getState()).thenReturn(LISTING);
        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decideAnApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(1, callbackResponse.getErrors().size());
        assertNotNull(callbackResponse);
    }

    @Test
    void should_throw_no_make_an_applications_list() {

        assertThatThrownBy(() -> decideAnApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Make an applications list not present");
    }

    @Test
    void should_throw_no_decision() {

        DynamicList makeAnApplicationsList = new DynamicList(
            new Value("1", "Legal representative : Application 1"),
            Arrays.asList(new Value("1", "Legal representative : Application 1")));
        when(asylumCase.read(MAKE_AN_APPLICATIONS_LIST, DynamicList.class))
            .thenReturn(Optional.of(makeAnApplicationsList));

        assertThatThrownBy(() -> decideAnApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("No application decision is present");
    }

    @Test
    void should_throw_no_decision_reason() {

        final DynamicList makeAnApplicationsList = new DynamicList(
            new Value("1", "Legal representative : Application 1"),
            Arrays.asList(new Value("1", "Legal representative : Application 1")));
        when(asylumCase.read(MAKE_AN_APPLICATIONS_LIST, DynamicList.class))
            .thenReturn(Optional.of(makeAnApplicationsList));
        when(asylumCase.read(MAKE_AN_APPLICATION_DECISION, MakeAnApplicationDecision.class))
            .thenReturn(Optional.of(GRANTED));

        assertThatThrownBy(() -> decideAnApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("No application decision reason is present");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> decideAnApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> decideAnApplicationHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideAnApplicationHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideAnApplicationHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideAnApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = decideAnApplicationHandler.canHandle(callbackStage, callback);

                if ((event == Event.DECIDE_AN_APPLICATION)
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void setDecisionInfoTest() {
        MakeAnApplication makeAnApplication = new MakeAnApplication();
        decideAnApplicationHandler.setDecisionInfo(makeAnApplication, "a decision", "a reason", "a date", "a maker role");
        assertThat(makeAnApplication.getDecision()).isEqualTo("a decision");
        assertThat(makeAnApplication.getDecisionReason()).isEqualTo("a reason");
        assertThat(makeAnApplication.getDecisionDate()).isEqualTo("a date");
        assertThat(makeAnApplication.getDecisionMaker()).isEqualTo("a maker role");
    }
}
