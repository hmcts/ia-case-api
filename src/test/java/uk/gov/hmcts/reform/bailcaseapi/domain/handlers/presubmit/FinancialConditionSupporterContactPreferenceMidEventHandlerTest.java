package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ContactPreference.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class FinancialConditionSupporterContactPreferenceMidEventHandlerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    private FinancialConditionSupporterContactPreferenceMidEventHandler
        financialConditionSupporterContactPreferenceMidEventHandler;

    private static final String SUPPORTER_1_CONTACT_PREF_PAGE_ID = "supporterContactDetails";
    private static final String SUPPORTER_2_CONTACT_PREF_PAGE_ID = "supporter2ContactDetails";
    private static final String SUPPORTER_3_CONTACT_PREF_PAGE_ID = "supporter3ContactDetails";
    private static final String SUPPORTER_4_CONTACT_PREF_PAGE_ID = "supporter4ContactDetails";
    private static final String EMAIL_REQUIRED_ERROR = "Email is required.";
    private static final String PHONE_REQUIRED_ERROR = "At least one phone type is required.";

    @BeforeEach
    public void setUp() {
        financialConditionSupporterContactPreferenceMidEventHandler =
            new FinancialConditionSupporterContactPreferenceMidEventHandler();
    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_add_email_error_message_when_only_telephone_selected(Event event) {

        List<ContactPreference> listOfContactPreferences =
            List.of(TELEPHONE);

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getPageId()).thenReturn(SUPPORTER_1_CONTACT_PREF_PAGE_ID);
        when(bailCase.read(SUPPORTER_CONTACT_DETAILS))
            .thenReturn(Optional.of(listOfContactPreferences));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            financialConditionSupporterContactPreferenceMidEventHandler
                .handle(PreSubmitCallbackStage.MID_EVENT, callback);

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors).contains(EMAIL_REQUIRED_ERROR);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_add_email_error_message_when_only_mobile_selected(Event event) {

        List<ContactPreference> listOfContactPreferences =
            List.of(MOBILE);

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getPageId()).thenReturn(SUPPORTER_2_CONTACT_PREF_PAGE_ID);
        when(bailCase.read(SUPPORTER_2_CONTACT_DETAILS))
            .thenReturn(Optional.of(listOfContactPreferences));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            financialConditionSupporterContactPreferenceMidEventHandler
                .handle(PreSubmitCallbackStage.MID_EVENT, callback);

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors).contains(EMAIL_REQUIRED_ERROR);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_add_email_error_message_when_telephone_and_mobile_selected(Event event) {

        List<ContactPreference> listOfContactPreferences =
            List.of(TELEPHONE, MOBILE);

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getPageId()).thenReturn(SUPPORTER_3_CONTACT_PREF_PAGE_ID);
        when(bailCase.read(SUPPORTER_3_CONTACT_DETAILS))
            .thenReturn(Optional.of(listOfContactPreferences));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            financialConditionSupporterContactPreferenceMidEventHandler
                .handle(PreSubmitCallbackStage.MID_EVENT, callback);

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors).contains(EMAIL_REQUIRED_ERROR);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_add_email_error_message_when_only_email_selected(Event event) {

        List<ContactPreference> listOfContactPreferences =
            List.of(EMAIL);

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getPageId()).thenReturn(SUPPORTER_4_CONTACT_PREF_PAGE_ID);
        when(bailCase.read(SUPPORTER_4_CONTACT_DETAILS))
            .thenReturn(Optional.of(listOfContactPreferences));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            financialConditionSupporterContactPreferenceMidEventHandler
                .handle(PreSubmitCallbackStage.MID_EVENT, callback);

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors).contains(PHONE_REQUIRED_ERROR);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_add_no_errors_when_email_and_telephone_selected(Event event) {

        List<ContactPreference> listOfContactPreferences =
            List.of(EMAIL, TELEPHONE);

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getPageId()).thenReturn(SUPPORTER_1_CONTACT_PREF_PAGE_ID);
        when(bailCase.read(SUPPORTER_CONTACT_DETAILS))
            .thenReturn(Optional.of(listOfContactPreferences));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            financialConditionSupporterContactPreferenceMidEventHandler
                .handle(PreSubmitCallbackStage.MID_EVENT, callback);

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(0);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_add_no_errors_when_email_and_mobile_selected(Event event) {

        List<ContactPreference> listOfContactPreferences =
            List.of(EMAIL, MOBILE);

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getPageId()).thenReturn(SUPPORTER_2_CONTACT_PREF_PAGE_ID);
        when(bailCase.read(SUPPORTER_2_CONTACT_DETAILS))
            .thenReturn(Optional.of(listOfContactPreferences));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            financialConditionSupporterContactPreferenceMidEventHandler
                .handle(PreSubmitCallbackStage.MID_EVENT, callback);

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(0);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_add_no_errors_when_email_and_telephone_and_mobile_selected(Event event) {

        List<ContactPreference> listOfContactPreferences =
            List.of(EMAIL, TELEPHONE, MOBILE);

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getPageId()).thenReturn(SUPPORTER_3_CONTACT_PREF_PAGE_ID);
        when(bailCase.read(SUPPORTER_3_CONTACT_DETAILS))
            .thenReturn(Optional.of(listOfContactPreferences));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            financialConditionSupporterContactPreferenceMidEventHandler
                .handle(PreSubmitCallbackStage.MID_EVENT, callback);

        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(0);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> financialConditionSupporterContactPreferenceMidEventHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback_for_all_events() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getPageId()).thenReturn(SUPPORTER_1_CONTACT_PREF_PAGE_ID);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = financialConditionSupporterContactPreferenceMidEventHandler
                    .canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.MID_EVENT
                    && (callback.getEvent() == Event.START_APPLICATION
                        || callback.getEvent() == Event.EDIT_BAIL_APPLICATION
                        || callback.getEvent() == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT
                        || callback.getEvent() == Event.MAKE_NEW_APPLICATION)
                    && callback.getPageId().equals(SUPPORTER_1_CONTACT_PREF_PAGE_ID)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> financialConditionSupporterContactPreferenceMidEventHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> financialConditionSupporterContactPreferenceMidEventHandler
            .canHandle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> financialConditionSupporterContactPreferenceMidEventHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> financialConditionSupporterContactPreferenceMidEventHandler
            .handle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
