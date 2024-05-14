package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ContactPreference.WANTS_EMAIL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SourceOfAppeal;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class EjpContactPreferenceFieldsHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    private final String emailUnrep = "unrep@example.com";
    private final String mobileNumberUnrep = "07781122334";
    private EjpContactPreferenceFieldsHandler ejpContactPreferenceFieldsHandler;

    @BeforeEach
    public void setUp() {

        ejpContactPreferenceFieldsHandler = new EjpContactPreferenceFieldsHandler();
        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class)).thenReturn(Optional.of(SourceOfAppeal.TRANSFERRED_FROM_UPPER_TRIBUNAL));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL", "EDIT_APPEAL"
    })
    void handler_should_write_emailUnrep_and_mobileNumberUnrep_to_email_and_mobileNumber_fields_for_unrep_ejp(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(EMAIL_UNREP, String.class)).thenReturn(Optional.of(emailUnrep));
        when(asylumCase.read(MOBILE_NUMBER_UNREP, String.class)).thenReturn(Optional.of(mobileNumberUnrep));
        when(asylumCase.read(IS_LEGALLY_REPRESENTED_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> response =
            ejpContactPreferenceFieldsHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, times(1)).write(EMAIL, emailUnrep);
        verify(asylumCase, times(1)).write(MOBILE_NUMBER, mobileNumberUnrep);
    }

    @Test
    void handler_should_clear_unrep_contact_preference_fields_when_edited_to_repped_case() {
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(IS_LEGALLY_REPRESENTED_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> response =
            ejpContactPreferenceFieldsHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, times(1)).clear(EMAIL_UNREP);
        verify(asylumCase, times(1)).clear(MOBILE_NUMBER_UNREP);
        verify(asylumCase, times(1)).clear(CONTACT_PREFERENCE_UNREP);
    }

    @ParameterizedTest
    @EnumSource(value = ContactPreference.class, names = { "WANTS_EMAIL", "WANTS_SMS" })
    void handler_should_clear_email_or_sms_depending_on_contactPreference_field_selected(ContactPreference preference) {
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(IS_LEGALLY_REPRESENTED_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)).thenReturn(Optional.of(preference));

        PreSubmitCallbackResponse<AsylumCase> response =
            ejpContactPreferenceFieldsHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();

        if (preference.equals(WANTS_EMAIL)) {
            verify(asylumCase, times(1)).clear(MOBILE_NUMBER);

        } else {
            verify(asylumCase, times(1)).clear(EMAIL);
        }
    }

    @ParameterizedTest
    @EnumSource(value = Event.class)
    void it_can_handle_callback(Event event) {

        for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
            when(callback.getEvent()).thenReturn(event);
            boolean canHandle = ejpContactPreferenceFieldsHandler.canHandle(stage, callback);

            if (stage == ABOUT_TO_SUBMIT && (event == Event.START_APPEAL || event == Event.EDIT_APPEAL)) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = Event.class)
    void should_not_allow_null_arguments(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertThatThrownBy(() -> ejpContactPreferenceFieldsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ejpContactPreferenceFieldsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ejpContactPreferenceFieldsHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ejpContactPreferenceFieldsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class)
    void handling_should_throw_if_cannot_actually_handle(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertThatThrownBy(() -> ejpContactPreferenceFieldsHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> ejpContactPreferenceFieldsHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
