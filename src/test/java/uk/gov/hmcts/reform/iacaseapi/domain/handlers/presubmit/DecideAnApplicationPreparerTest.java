package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DecideAnApplicationPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Mock
    private DateProvider dateProvider;
    @Mock
    private UserDetails userDetails;
    @Mock
    private UserDetailsHelper userDetailsHelper;

    private DecideAnApplicationPreparer decideAnApplicationPreparer;

    @BeforeEach
    public void setUp() {

        decideAnApplicationPreparer = new DecideAnApplicationPreparer(userDetails, userDetailsHelper);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.DECIDE_AN_APPLICATION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_handle_the_about_to_start() {

        when(dateProvider.now()).thenReturn(LocalDate.MAX);

        List<IdValue<Document>> evidence =
            List.of(new IdValue<>("1", new Document("url", "url", "FileName")));
        MakeAnApplication makeAnApplication =
            new MakeAnApplication("Legal representative", "Update appeal details", "A reason to update appeal details",
                evidence, dateProvider.now().toString(), "Pending",
                State.LISTING.toString());
        makeAnApplication.setApplicantRole("caseworker-ia-caseofficer");
        final List<IdValue<MakeAnApplication>> makeAnApplications =
            List.of(new IdValue<>("1", makeAnApplication));

        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.ADMIN_OFFICER);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decideAnApplicationPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
            .write(MAKE_AN_APPLICATIONS_LIST,
                new DynamicList(new Value("1", "Legal representative : Application 1"),
                    List.of(new Value("1", "Legal representative : Application 1"))));
        verify(asylumCase, never()).write(eq(REMOVAL_OF_24W_DECISION_DECISION_MAKER), anyString());
        verify(asylumCase).clear(REMOVAL_OF_24W_DECISION_DECISION_MAKER);
    }

    @ParameterizedTest
    @EnumSource(value = UserRoleLabel.class, names = {"JUDGE", "TRIBUNAL_CASEWORKER"})
    void should_write_user_name_if_logged_in_valid_user(UserRoleLabel userRoleLabel) {

        when(dateProvider.now()).thenReturn(LocalDate.MAX);

        List<IdValue<Document>> evidence =
            List.of(new IdValue<>("1", new Document("url", "url", "FileName")));
        MakeAnApplication makeAnApplication =
            new MakeAnApplication("Legal representative", "Update appeal details", "A reason to update appeal details",
                evidence, dateProvider.now().toString(), "Pending",
                State.LISTING.toString());
        makeAnApplication.setApplicantRole("caseworker-ia-caseofficer");
        final List<IdValue<MakeAnApplication>> makeAnApplications =
            List.of(new IdValue<>("1", makeAnApplication));

        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(userRoleLabel);
        when(userDetails.getForenameAndSurname()).thenReturn("Judge Judy");
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decideAnApplicationPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
            .write(MAKE_AN_APPLICATIONS_LIST,
                new DynamicList(new Value("1", "Legal representative : Application 1"),
                    List.of(new Value("1", "Legal representative : Application 1"))));
        verify(asylumCase).write(REMOVAL_OF_24W_DECISION_DECISION_MAKER, "Judge Judy");
        verify(asylumCase, never()).clear(REMOVAL_OF_24W_DECISION_DECISION_MAKER);
    }


    @ParameterizedTest
    @EnumSource(value = UserRoleLabel.class, names = {"JUDGE", "TRIBUNAL_CASEWORKER"}, mode = EnumSource.Mode.EXCLUDE)
    void should_clear_user_name_if_logged_in_non_valid_user(UserRoleLabel userRoleLabel) {

        when(dateProvider.now()).thenReturn(LocalDate.MAX);

        List<IdValue<Document>> evidence =
            List.of(new IdValue<>("1", new Document("url", "url", "FileName")));
        MakeAnApplication makeAnApplication =
            new MakeAnApplication("Legal representative", "Update appeal details", "A reason to update appeal details",
                evidence, dateProvider.now().toString(), "Pending",
                State.LISTING.toString());
        makeAnApplication.setApplicantRole("caseworker-ia-caseofficer");
        final List<IdValue<MakeAnApplication>> makeAnApplications =
            List.of(new IdValue<>("1", makeAnApplication));

        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(userRoleLabel);
        when(userDetails.getForenameAndSurname()).thenReturn("Judge Judy");
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decideAnApplicationPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
            .write(MAKE_AN_APPLICATIONS_LIST,
                new DynamicList(new Value("1", "Legal representative : Application 1"),
                    List.of(new Value("1", "Legal representative : Application 1"))));
        verify(asylumCase, never()).write(eq(REMOVAL_OF_24W_DECISION_DECISION_MAKER), any());
        verify(asylumCase).clear(REMOVAL_OF_24W_DECISION_DECISION_MAKER);
    }

    @Test
    void should_return_error_if_no_applications_to_decide() {

        final List<IdValue<MakeAnApplication>> makeAnApplications = Collections.emptyList();
        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decideAnApplicationPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).contains("There are no applications to decide.");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> decideAnApplicationPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> decideAnApplicationPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideAnApplicationPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideAnApplicationPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideAnApplicationPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = decideAnApplicationPreparer.canHandle(callbackStage, callback);

                if ((event == Event.DECIDE_AN_APPLICATION)
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }
}
