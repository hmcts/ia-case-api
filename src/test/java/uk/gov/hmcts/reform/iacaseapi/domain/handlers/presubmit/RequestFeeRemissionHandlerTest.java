package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RemissionDetailsAppender;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class RequestFeeRemissionHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Mock private FeatureToggler featureToggler;
    @Mock private IdValue<RemissionDetails> previousRemissionDetailsById;
    @Mock private RemissionDetailsAppender remissionDetailsAppender;

    private RequestFeeRemissionHandler requestFeeRemissionHandler;

    @BeforeEach
    void setUp() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        requestFeeRemissionHandler = new RequestFeeRemissionHandler(featureToggler, remissionDetailsAppender);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> requestFeeRemissionHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @MethodSource("remissionClaimsTestData")
    void handle_should_return_new_and_previous_remission_details(RemissionType remissionType, String remissionClaim) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.REQUEST_FEE_REMISSION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(remissionType));
        when(asylumCase.read(REMISSION_CLAIM, String.class)).thenReturn(Optional.of(remissionClaim));
        when(remissionDetailsAppender.getRemissions()).thenReturn(Arrays.asList(previousRemissionDetailsById));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = requestFeeRemissionHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(PREVIOUS_REMISSION_DETAILS, remissionDetailsAppender.getRemissions());
        verify(asylumCase, times(1)).write(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.YES);


        if (remissionType == HO_WAIVER_REMISSION) {

            switch (remissionClaim) {
                case "asylumSupport":

                    assertAsylumSupportRemissionDetails(asylumCase);
                    break;

                case "legalAid":

                    assertLegalAidRemissionDetails(asylumCase);
                    break;

                case "section17":

                    assertSection17RemissionDetails(asylumCase);
                    break;

                case "section20":

                    assertSection20RemissionDetails(asylumCase);
                    break;

                case "homeOfficeWaiver":

                    assertHomeOfficeWaiverRemissionDetails(asylumCase);
                    break;

                default:
                    break;
            }
        } else if (remissionType == HELP_WITH_FEES) {

            assertHelpWithFees(asylumCase);
        } else if (remissionType == EXCEPTIONAL_CIRCUMSTANCES_REMISSION) {

            assertExceptionalCircumstancesRemissionDetails(asylumCase);
        }

        verify(asylumCase, times(1)).clear(REMISSION_DECISION);
        verify(asylumCase, times(1)).clear(AMOUNT_REMITTED);
        verify(asylumCase, times(1)).clear(AMOUNT_LEFT_TO_PAY);
        verify(asylumCase, times(1)).clear(REMISSION_DECISION_REASON);
        verify(asylumCase, times(1)).clear(REMISSION_TYPE);
    }

    private static Stream<Arguments> remissionClaimsTestData() {

        return Stream.of(
            Arguments.of(HO_WAIVER_REMISSION, "asylumSupport"),
            Arguments.of(HO_WAIVER_REMISSION, "legalAid"),
            Arguments.of(HO_WAIVER_REMISSION, "section17"),
            Arguments.of(HO_WAIVER_REMISSION, "section20"),
            Arguments.of(HO_WAIVER_REMISSION, "homeOfficeWaiver"),
            Arguments.of(HELP_WITH_FEES, "Help with Fees"),
            Arguments.of(EXCEPTIONAL_CIRCUMSTANCES_REMISSION, "Exceptional circumstances")
        );
    }

    private void assertAsylumSupportRemissionDetails(AsylumCase asylumCase) {

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Asylum support");
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
        verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);
        verify(asylumCase, times(1)).clear(HELP_WITH_FEES_REFERENCE_NUMBER);
        verify(asylumCase, times(1)).clear(EXCEPTIONAL_CIRCUMSTANCES);
        verify(asylumCase, times(1)).clear(REMISSION_EC_EVIDENCE_DOCUMENTS);
    }

    private void assertLegalAidRemissionDetails(AsylumCase asylumCase) {

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Legal Aid");
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);
        verify(asylumCase, times(1)).clear(HELP_WITH_FEES_REFERENCE_NUMBER);
        verify(asylumCase, times(1)).clear(EXCEPTIONAL_CIRCUMSTANCES);
        verify(asylumCase, times(1)).clear(REMISSION_EC_EVIDENCE_DOCUMENTS);
    }

    private void assertSection17RemissionDetails(AsylumCase asylumCase) {

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Section 17");
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
        verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);
        verify(asylumCase, times(1)).clear(HELP_WITH_FEES_REFERENCE_NUMBER);
        verify(asylumCase, times(1)).clear(EXCEPTIONAL_CIRCUMSTANCES);
        verify(asylumCase, times(1)).clear(REMISSION_EC_EVIDENCE_DOCUMENTS);
    }

    private void assertSection20RemissionDetails(AsylumCase asylumCase) {

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Section 20");
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
        verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);
        verify(asylumCase, times(1)).clear(HELP_WITH_FEES_REFERENCE_NUMBER);
        verify(asylumCase, times(1)).clear(EXCEPTIONAL_CIRCUMSTANCES);
        verify(asylumCase, times(1)).clear(REMISSION_EC_EVIDENCE_DOCUMENTS);
    }

    private void assertHomeOfficeWaiverRemissionDetails(AsylumCase asylumCase) {

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Home Office fee waiver");
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
        verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
        verify(asylumCase, times(1)).clear(HELP_WITH_FEES_REFERENCE_NUMBER);
        verify(asylumCase, times(1)).clear(EXCEPTIONAL_CIRCUMSTANCES);
        verify(asylumCase, times(1)).clear(REMISSION_EC_EVIDENCE_DOCUMENTS);
    }

    private void assertHelpWithFees(AsylumCase asylumCase) {

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Help with Fees");
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
        verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
        verify(asylumCase, times(1)).clear(EXCEPTIONAL_CIRCUMSTANCES);
        verify(asylumCase, times(1)).clear(REMISSION_EC_EVIDENCE_DOCUMENTS);
    }

    private void assertExceptionalCircumstancesRemissionDetails(AsylumCase asylumCase) {

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Exceptional circumstances");
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
        verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
        verify(asylumCase, times(1)).clear(HELP_WITH_FEES_REFERENCE_NUMBER);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = requestFeeRemissionHandler.canHandle(callbackStage, callback);

                if (event == Event.REQUEST_FEE_REMISSION
                    && callbackStage == ABOUT_TO_SUBMIT) {

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

        assertThatThrownBy(() -> requestFeeRemissionHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestFeeRemissionHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
