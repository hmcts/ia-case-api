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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;

import java.util.Arrays;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class PartyIdBailHandlerTest {

    private static final String APPELLANT_PARTY_ID_VALUE = "111222333";
    private static final String LEGAL_REP_INDIVIDUAL_PARTY_ID_VALUE = "222111333";
    private static final String LEGAL_REP_ORGANISATION_PARTY_ID_VALUE = "333222111";
    private static final String SPONSOR_PARTY_ID_VALUE = "444555666";

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Captor
    private ArgumentCaptor<String> partyId;

    private uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.PartyIdBailHandler partyIdBailHandler;

    @BeforeEach
    public void setUp() {
        partyIdBailHandler = new uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.PartyIdBailHandler();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPELLANT_PARTY_ID, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_INDIVIDUAL_PARTY_ID, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_ORGANISATION_PARTY_ID, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.empty());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL",
        "EDIT_APPEAL",
        "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_correctly_generate_party_ids_for_in_country_appeals(Event event) {
        when(callback.getEvent()).thenReturn(event);

        if (Arrays.asList(START_APPEAL, EDIT_APPEAL).contains(event)) {
            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                    partyIdBailHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(asylumCase, callbackResponse.getData());

            verify(asylumCase, times(1)).write(eq(APPELLANT_PARTY_ID), partyId.capture());
            assertNotNull(partyId.getValue());

            verify(asylumCase, times(1))
                .write(eq(LEGAL_REP_INDIVIDUAL_PARTY_ID), partyId.capture());
            assertNotNull(partyId.getValue());

            verify(asylumCase, times(1))
                .write(eq(LEGAL_REP_ORGANISATION_PARTY_ID), partyId.capture());
            assertNotNull(partyId.getValue());
        }

        verify(asylumCase, never()).write(eq(SPONSOR_PARTY_ID), anyString());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL",
        "EDIT_APPEAL",
        "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_correctly_generate_party_ids_for_out_of_country_appeals(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = partyIdBailHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        if (Arrays.asList(START_APPEAL, EDIT_APPEAL).contains(event)) {
            assertNotNull(callbackResponse);
            assertEquals(asylumCase, callbackResponse.getData());

            verify(asylumCase, times(1)).write(eq(APPELLANT_PARTY_ID), partyId.capture());
            assertNotNull(partyId.getValue());

            verify(asylumCase, times(1))
                .write(eq(LEGAL_REP_INDIVIDUAL_PARTY_ID), partyId.capture());
            assertNotNull(partyId.getValue());

            verify(asylumCase, times(1))
                .write(eq(LEGAL_REP_ORGANISATION_PARTY_ID), partyId.capture());
            assertNotNull(partyId.getValue());
        }

        verify(asylumCase, never()).write(eq(SPONSOR_PARTY_ID), anyString());

        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        callbackResponse = partyIdBailHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(SPONSOR_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL",
        "EDIT_APPEAL",
        "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_not_set_party_id_if_already_set(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_PARTY_ID, String.class)).thenReturn(Optional.of(APPELLANT_PARTY_ID_VALUE));
        when(asylumCase.read(LEGAL_REP_INDIVIDUAL_PARTY_ID, String.class))
            .thenReturn(Optional.of(LEGAL_REP_INDIVIDUAL_PARTY_ID_VALUE));
        when(asylumCase.read(LEGAL_REP_ORGANISATION_PARTY_ID, String.class))
            .thenReturn(Optional.of(LEGAL_REP_ORGANISATION_PARTY_ID_VALUE));
        when(asylumCase.read(SPONSOR_PARTY_ID, String.class)).thenReturn(Optional.of(SPONSOR_PARTY_ID_VALUE));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            partyIdBailHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(APPELLANT_PARTY_ID), anyString());
        verify(asylumCase, never()).write(eq(LEGAL_REP_INDIVIDUAL_PARTY_ID), anyString());
        verify(asylumCase, never()).write(eq(LEGAL_REP_ORGANISATION_PARTY_ID), anyString());
        verify(asylumCase, never()).write(eq(SPONSOR_PARTY_ID), anyString());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL",
        "EDIT_APPEAL"
    })
    void should_set_appellant_sponsor_party_id_for_aip_journey_with_out_of_country_appeals(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                partyIdBailHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());
        verify(asylumCase, times(1)).write(eq(SPONSOR_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());

        verify(asylumCase, never()).write(eq(LEGAL_REP_INDIVIDUAL_PARTY_ID), anyString());
        verify(asylumCase, never()).write(eq(LEGAL_REP_ORGANISATION_PARTY_ID), anyString());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL",
        "EDIT_APPEAL"
    })
    void should_not_set_legal_rep_party_id_for_aip_journey(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                partyIdBailHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(APPELLANT_PARTY_ID), partyId.capture());
        assertNotNull(partyId.getValue());

        verify(asylumCase, never()).write(eq(LEGAL_REP_INDIVIDUAL_PARTY_ID), anyString());
        verify(asylumCase, never()).write(eq(LEGAL_REP_ORGANISATION_PARTY_ID), anyString());
        verify(asylumCase, never()).write(eq(SPONSOR_PARTY_ID), anyString());
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
}
