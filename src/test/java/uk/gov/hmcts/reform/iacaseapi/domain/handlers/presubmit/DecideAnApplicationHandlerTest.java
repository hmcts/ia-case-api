package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECIDE_AN_APPLICATION_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_APPLICATIONS_TO_DECIDE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATIONS_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATION_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATION_DECISION_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATION_FIELDS;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationDecision;
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

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DecideAnApplicationHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Mock
    private DateProvider dateProvider;
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private UserDetailsProvider userDetailsProvider;

    private DecideAnApplicationHandler decideAnApplicationHandler;

    @BeforeEach
    public void setUp() {

        decideAnApplicationHandler = new DecideAnApplicationHandler(dateProvider, userDetailsProvider, featureToggler);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.DECIDE_AN_APPLICATION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("make-an-application-feature", false)).thenReturn(true);
    }

    @Test
    void should_handle_the_about_to_submit() {

        when(dateProvider.now()).thenReturn(LocalDate.MAX);

        final DynamicList makeAnApplicationsList = new DynamicList(
            new Value("1", "Legal representative : Application 1"),
            Arrays.asList(new Value("1", "Legal representative : Application 1")));
        List<IdValue<Document>> evidence =
            Arrays.asList(new IdValue<>("1",
                new Document("http://localhost/documents/123456",
                    "http://localhost/documents/123456",
                    "DocumentName.pdf")));
        MakeAnApplication makeAnApplication =
            new MakeAnApplication("Legal representative", "Update appeal details", "A reason to update appeal details",
                evidence, dateProvider.now().toString(), "Pending",
                State.LISTING.toString());
        makeAnApplication.setApplicantRole("caseworker-ia-legalrep-solicitor");
        List<IdValue<MakeAnApplication>> makeAnApplications = Arrays.asList(new IdValue<>("1", makeAnApplication));

        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));

        when(userDetailsProvider.getLoggedInUserRoleLabel()).thenReturn(UserRoleLabel.TRIBUNAL_CASEWORKER);
        when(asylumCase.read(MAKE_AN_APPLICATIONS_LIST, DynamicList.class))
            .thenReturn(Optional.of(makeAnApplicationsList));
        when(asylumCase.read(MAKE_AN_APPLICATION_DECISION, MakeAnApplicationDecision.class))
            .thenReturn(Optional.of(MakeAnApplicationDecision.GRANTED));
        when(asylumCase.read(MAKE_AN_APPLICATION_DECISION_REASON, String.class))
            .thenReturn(Optional.of("A reason of the decision"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decideAnApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(DECIDE_AN_APPLICATION_ID, "1");
        verify(asylumCase, times(1)).write(HAS_APPLICATIONS_TO_DECIDE, YesOrNo.NO);
        //verify(asylumCase, times(1)).write(MAKE_AN_APPLICATIONS, makeAnApplications);

        verify(asylumCase, times(1)).clear(MAKE_AN_APPLICATIONS_LIST);
        verify(asylumCase, times(1)).clear(MAKE_AN_APPLICATION_FIELDS);
        verify(asylumCase, times(1)).clear(MAKE_AN_APPLICATION_DECISION);
        verify(asylumCase, times(1)).clear(MAKE_AN_APPLICATION_DECISION_REASON);
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
            .thenReturn(Optional.of(MakeAnApplicationDecision.GRANTED));

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
}
