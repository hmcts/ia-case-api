package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_PARTY_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_SPONSOR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_INDIVIDUAL_PARTY_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_ORGANISATION_PARTY_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_PARTY_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.LIST_ASSIST_INTEGRATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AsylumFieldLegalRepNameFixer;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ListAssistIntegrationHandlerTest {

    private static final String WITNESS_1_PARTY_ID = "111222333";
    private static final String WITNESS_2_PARTY_ID = "222111333";

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private List<IdValue<WitnessDetails>> witnessDetails;
    @Mock
    private AsylumFieldLegalRepNameFixer asylumFieldLegalRepDataFixer;
    @Captor
    private ArgumentCaptor<String> appellantPartyIdCaptor;
    @Captor
    private ArgumentCaptor<String> legalRepIndividualPartyIdCaptor;
    @Captor
    private ArgumentCaptor<String> legalRepOrgPartyIdCaptor;
    @Captor
    private ArgumentCaptor<String> sponsorPartyIdCaptor;
    @Captor
    private ArgumentCaptor<List<IdValue<WitnessDetails>>> witnessDetailsCaptor;

    private Pattern partyIdRegexPattern;
    private ListAssistIntegrationHandler handler;

    @BeforeEach
    void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(LIST_ASSIST_INTEGRATION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        partyIdRegexPattern = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

        handler = new ListAssistIntegrationHandler(asylumFieldLegalRepDataFixer);
    }

    @Test
    void should_set_party_ids() {
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(HAS_SPONSOR, YesOrNo.class)).thenReturn(Optional.of(YES));

        WitnessDetails witnessDetails1 = new WitnessDetails(WITNESS_1_PARTY_ID, "witness1", "family1", NO);
        WitnessDetails witnessDetails2 = new WitnessDetails(WITNESS_2_PARTY_ID, "witness2", "family2", NO);
        witnessDetails = Arrays.asList(
            new IdValue<>("1", witnessDetails1),
            new IdValue<>("2", witnessDetails2)
        );
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumFieldLegalRepDataFixer, times(1)).fix(asylumCase);

        verify(asylumCase).write(eq(WITNESS_DETAILS), witnessDetailsCaptor.capture());
        verify(asylumCase).write(eq(APPELLANT_PARTY_ID), appellantPartyIdCaptor.capture());
        verify(asylumCase).write(eq(LEGAL_REP_INDIVIDUAL_PARTY_ID), legalRepIndividualPartyIdCaptor.capture());
        verify(asylumCase).write(eq(LEGAL_REP_ORGANISATION_PARTY_ID), legalRepOrgPartyIdCaptor.capture());
        verify(asylumCase).write(eq(SPONSOR_PARTY_ID), sponsorPartyIdCaptor.capture());

        assertEquals(WITNESS_1_PARTY_ID, witnessDetailsCaptor.getValue().get(0).getValue().getWitnessPartyId());
        assertEquals(WITNESS_2_PARTY_ID, witnessDetailsCaptor.getValue().get(1).getValue().getWitnessPartyId());
        assertTrue(partyIdRegexPattern.matcher(appellantPartyIdCaptor.getValue()).matches());
        assertTrue(partyIdRegexPattern.matcher(legalRepIndividualPartyIdCaptor.getValue()).matches());
        assertTrue(partyIdRegexPattern.matcher(legalRepOrgPartyIdCaptor.getValue()).matches());
        assertTrue(partyIdRegexPattern.matcher(sponsorPartyIdCaptor.getValue()).matches());
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> handler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
