package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class PartyIdBailHandlerTest {

    private static final String APPLICANT_PARTY_ID_VALUE = "111222333";
    private static final String LEGAL_REP_INDIVIDUAL_PARTY_ID_VALUE = "222333444";
    private static final String LEGAL_REP_ORGANISATION_PARTY_ID_VALUE = "444555666";
    private static final String SUPPORTER_1_PARTY_ID_VALUE = "100000000";
    private static final String SUPPORTER_2_PARTY_ID_VALUE = "200000000";
    private static final String SUPPORTER_3_PARTY_ID_VALUE = "300000000";
    private static final String SUPPORTER_4_PARTY_ID_VALUE = "400000000";

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Captor
    private ArgumentCaptor<String> partyId;

    private uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.PartyIdBailHandler partyIdBailHandler;

    @BeforeEach
    public void setUp() {
        partyIdBailHandler = new uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.PartyIdBailHandler();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);

        when(bailCase.read(APPLICANT_PARTY_ID, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_INDIVIDUAL_PARTY_ID, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_ORGANISATION_PARTY_ID, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(SUPPORTER_1_PARTY_ID, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(SUPPORTER_2_PARTY_ID, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(SUPPORTER_3_PARTY_ID, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(SUPPORTER_4_PARTY_ID, String.class)).thenReturn(Optional.empty());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPLICATION",
        "EDIT_BAIL_APPLICATION",
        "EDIT_BAIL_APPLICATION_AFTER_SUBMIT"
    })
    void should_correctly_generate_party_ids_repped_and_with_supporters_start_application(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_4, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            partyIdBailHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, times(1)).write(eq(APPLICANT_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());

        verify(bailCase, times(1)).write(eq(LEGAL_REP_INDIVIDUAL_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());

        verify(bailCase, times(1)).write(eq(LEGAL_REP_ORGANISATION_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());

        verify(bailCase, times(1)).write(eq(SUPPORTER_1_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());

        verify(bailCase, times(1)).write(eq(SUPPORTER_2_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());

        verify(bailCase, times(1)).write(eq(SUPPORTER_3_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());

        verify(bailCase, times(1)).write(eq(SUPPORTER_4_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPLICATION",
        "EDIT_BAIL_APPLICATION",
        "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
        "MAKE_NEW_APPLICATION"
    })
    void should_not_set_party_ids_if_already_set(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(bailCase.read(APPLICANT_PARTY_ID, String.class)).thenReturn(Optional.of(APPLICANT_PARTY_ID_VALUE));
        when(bailCase.read(LEGAL_REP_INDIVIDUAL_PARTY_ID, String.class))
            .thenReturn(Optional.of(LEGAL_REP_INDIVIDUAL_PARTY_ID_VALUE));
        when(bailCase.read(LEGAL_REP_ORGANISATION_PARTY_ID, String.class))
            .thenReturn(Optional.of(LEGAL_REP_ORGANISATION_PARTY_ID_VALUE));
        when(bailCase.read(SUPPORTER_1_PARTY_ID, String.class)).thenReturn(Optional.of(SUPPORTER_1_PARTY_ID_VALUE));
        when(bailCase.read(SUPPORTER_2_PARTY_ID, String.class)).thenReturn(Optional.of(SUPPORTER_2_PARTY_ID_VALUE));
        when(bailCase.read(SUPPORTER_3_PARTY_ID, String.class)).thenReturn(Optional.of(SUPPORTER_3_PARTY_ID_VALUE));
        when(bailCase.read(SUPPORTER_4_PARTY_ID, String.class)).thenReturn(Optional.of(SUPPORTER_4_PARTY_ID_VALUE));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            partyIdBailHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, never()).write(eq(APPLICANT_PARTY_ID), anyString());
        verify(bailCase, never()).write(eq(LEGAL_REP_INDIVIDUAL_PARTY_ID), anyString());
        verify(bailCase, never()).write(eq(LEGAL_REP_ORGANISATION_PARTY_ID), anyString());
        verify(bailCase, never()).write(eq(SUPPORTER_1_PARTY_ID), anyString());
        verify(bailCase, never()).write(eq(SUPPORTER_2_PARTY_ID), anyString());
        verify(bailCase, never()).write(eq(SUPPORTER_3_PARTY_ID), anyString());
        verify(bailCase, never()).write(eq(SUPPORTER_4_PARTY_ID), anyString());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "EDIT_BAIL_APPLICATION",
        "EDIT_BAIL_APPLICATION_AFTER_SUBMIT"
    })
    void should_clear_legal_rep_party_ids_for_unrep_applications(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(bailCase.read(LEGAL_REP_INDIVIDUAL_PARTY_ID, String.class)).thenReturn(Optional.of(
            LEGAL_REP_INDIVIDUAL_PARTY_ID_VALUE));
        when(bailCase.read(LEGAL_REP_ORGANISATION_PARTY_ID, String.class)).thenReturn(Optional.of(
            LEGAL_REP_ORGANISATION_PARTY_ID_VALUE));
        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            partyIdBailHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, times(1)).clear(LEGAL_REP_INDIVIDUAL_PARTY_ID);
        verify(bailCase, times(1)).clear(LEGAL_REP_ORGANISATION_PARTY_ID);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "EDIT_BAIL_APPLICATION",
        "EDIT_BAIL_APPLICATION_AFTER_SUBMIT"
    })
    void should_clear_supporter_1_2_3_4_party_ids_when_removed_during_edit_application(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(bailCase.read(SUPPORTER_1_PARTY_ID, String.class)).thenReturn(Optional.of(SUPPORTER_1_PARTY_ID_VALUE));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            partyIdBailHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, times(1)).clear(SUPPORTER_1_PARTY_ID);
        verify(bailCase, times(1)).clear(SUPPORTER_2_PARTY_ID);
        verify(bailCase, times(1)).clear(SUPPORTER_3_PARTY_ID);
        verify(bailCase, times(1)).clear(SUPPORTER_4_PARTY_ID);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "EDIT_BAIL_APPLICATION",
        "EDIT_BAIL_APPLICATION_AFTER_SUBMIT"
    })
    void should_clear_supporter_2_3_4_party_ids_when_removed_during_edit_application(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(bailCase.read(SUPPORTER_2_PARTY_ID, String.class)).thenReturn(Optional.of(SUPPORTER_2_PARTY_ID_VALUE));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            partyIdBailHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, times(1)).clear(SUPPORTER_2_PARTY_ID);
        verify(bailCase, times(1)).clear(SUPPORTER_3_PARTY_ID);
        verify(bailCase, times(1)).clear(SUPPORTER_4_PARTY_ID);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "EDIT_BAIL_APPLICATION",
        "EDIT_BAIL_APPLICATION_AFTER_SUBMIT"
    })
    void should_clear_supporter_3_4_party_ids_when_removed_during_edit_application(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(bailCase.read(SUPPORTER_3_PARTY_ID, String.class)).thenReturn(Optional.of(SUPPORTER_3_PARTY_ID_VALUE));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            partyIdBailHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, times(1)).clear(SUPPORTER_3_PARTY_ID);
        verify(bailCase, times(1)).clear(SUPPORTER_4_PARTY_ID);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "EDIT_BAIL_APPLICATION",
        "EDIT_BAIL_APPLICATION_AFTER_SUBMIT"
    })
    void should_clear_supporter_4_party_id_when_removed_during_edit_application(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(bailCase.read(SUPPORTER_4_PARTY_ID, String.class)).thenReturn(Optional.of(SUPPORTER_4_PARTY_ID_VALUE));
        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_4, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            partyIdBailHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, times(1)).clear(SUPPORTER_4_PARTY_ID);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> partyIdBailHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> partyIdBailHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {
        //invalid stage
        assertThatThrownBy(() -> partyIdBailHandler.handle(ABOUT_TO_START, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);

        //invalid event
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION);
        assertThatThrownBy(() -> partyIdBailHandler.handle(ABOUT_TO_SUBMIT, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);
    }
}
