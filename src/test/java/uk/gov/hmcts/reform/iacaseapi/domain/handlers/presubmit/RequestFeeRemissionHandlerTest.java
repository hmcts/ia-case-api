package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.PA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.APPROVED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.PARTIALLY_APPROVED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.REJECTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RemissionDetailsAppender;

import static java.util.Collections.singletonList;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class RequestFeeRemissionHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document document;
    @Mock private IdValue<Document> previousDocuments;

    @Mock private FeatureToggler featureToggler;

    @Mock private UserDetails userDetails;

    @Mock private UserDetailsHelper userDetailsHelper;

    private RequestFeeRemissionHandler requestFeeRemissionHandler;

    @BeforeEach
    void setUp() {
        RemissionDetailsAppender remissionDetailsAppender = new RemissionDetailsAppender();
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        requestFeeRemissionHandler = new RequestFeeRemissionHandler(featureToggler, remissionDetailsAppender,
            userDetails, userDetailsHelper);
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.ADMIN_OFFICER);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> requestFeeRemissionHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @MethodSource("remissionClaimsTestData")
    void handle_should_return_new_and_previous_remission_details_asylum_support(
        RemissionType remissionType,
        String remissionClaim,
        AppealType appealType,
        RemissionDecision remissionDecision
    ) {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        when(asylumCase.read(FEE_REMISSION_TYPE, String.class)).thenReturn(Optional.of("Asylum support"));
        when(asylumCase.read(ASYLUM_SUPPORT_REFERENCE, String.class)).thenReturn(Optional.of("123456"));
        when(asylumCase.read(ASYLUM_SUPPORT_DOCUMENT)).thenReturn(Optional.of(document));

        when(asylumCase.read(TEMP_PREVIOUS_REMISSION_DETAILS))
                .thenReturn(Optional.of(singletonList(new IdValue<>("123", mock(RemissionDetails.class)))));

        when(callback.getEvent()).thenReturn(Event.REQUEST_FEE_REMISSION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(remissionType));
        when(asylumCase.read(REMISSION_CLAIM, String.class)).thenReturn(Optional.of(remissionClaim));

        mockRemissionDecision(remissionDecision);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = requestFeeRemissionHandler.handle(ABOUT_TO_SUBMIT, callback);

        verifyTestResults(remissionType, remissionClaim, callbackResponse);
    }

    @ParameterizedTest
    @MethodSource("remissionClaimsTestData")
    void handle_should_return_new_and_previous_remission_details_legal_aid(
        RemissionType remissionType,
        String remissionClaim,
        AppealType appealType,
        RemissionDecision remissionDecision
    ) {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        when(asylumCase.read(FEE_REMISSION_TYPE, String.class)).thenReturn(Optional.of("Legal Aid"));
        when(asylumCase.read(LEGAL_AID_ACCOUNT_NUMBER, String.class)).thenReturn(Optional.of("123456"));

        when(asylumCase.read(TEMP_PREVIOUS_REMISSION_DETAILS))
                .thenReturn(Optional.of(singletonList(new IdValue<>("123", mock(RemissionDetails.class)))));

        when(callback.getEvent()).thenReturn(Event.REQUEST_FEE_REMISSION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(remissionType));
        when(asylumCase.read(REMISSION_CLAIM, String.class)).thenReturn(Optional.of(remissionClaim));

        mockRemissionDecision(remissionDecision);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = requestFeeRemissionHandler.handle(ABOUT_TO_SUBMIT, callback);

        verifyTestResults(remissionType, remissionClaim, callbackResponse);
    }

    @ParameterizedTest
    @MethodSource("remissionClaimsTestData")
    void handle_should_return_new_and_previous_remission_details_section_17(
        RemissionType remissionType,
        String remissionClaim,
        AppealType appealType,
        RemissionDecision remissionDecision
    ) {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        when(asylumCase.read(FEE_REMISSION_TYPE, String.class)).thenReturn(Optional.of("Section 17"));
        when(asylumCase.read(SECTION17_DOCUMENT)).thenReturn(Optional.of(document));

        when(asylumCase.read(TEMP_PREVIOUS_REMISSION_DETAILS))
                .thenReturn(Optional.of(singletonList(new IdValue<>("123", mock(RemissionDetails.class)))));

        when(callback.getEvent()).thenReturn(Event.REQUEST_FEE_REMISSION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(remissionType));
        when(asylumCase.read(REMISSION_CLAIM, String.class)).thenReturn(Optional.of(remissionClaim));

        mockRemissionDecision(remissionDecision);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = requestFeeRemissionHandler.handle(ABOUT_TO_SUBMIT, callback);

        verifyTestResults(remissionType, remissionClaim, callbackResponse);
    }

    @ParameterizedTest
    @MethodSource("remissionClaimsTestData")
    void handle_should_return_new_and_previous_remission_details_section_20(
        RemissionType remissionType,
        String remissionClaim,
        AppealType appealType,
        RemissionDecision remissionDecision
    ) {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        when(asylumCase.read(FEE_REMISSION_TYPE, String.class)).thenReturn(Optional.of("Section 20"));
        when(asylumCase.read(SECTION20_DOCUMENT)).thenReturn(Optional.of(document));

        when(asylumCase.read(TEMP_PREVIOUS_REMISSION_DETAILS))
                .thenReturn(Optional.of(singletonList(new IdValue<>("123", mock(RemissionDetails.class)))));

        when(callback.getEvent()).thenReturn(Event.REQUEST_FEE_REMISSION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(remissionType));
        when(asylumCase.read(REMISSION_CLAIM, String.class)).thenReturn(Optional.of(remissionClaim));

        mockRemissionDecision(remissionDecision);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = requestFeeRemissionHandler.handle(ABOUT_TO_SUBMIT, callback);

        verifyTestResults(remissionType, remissionClaim, callbackResponse);
    }

    @ParameterizedTest
    @MethodSource("remissionClaimsTestData")
    void handle_should_return_new_and_previous_remission_details_home_office_waiver(
        RemissionType remissionType,
        String remissionClaim,
        AppealType appealType,
        RemissionDecision remissionDecision
    ) {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        when(asylumCase.read(FEE_REMISSION_TYPE, String.class)).thenReturn(Optional.of("Home Office fee waiver"));
        when(asylumCase.read(HOME_OFFICE_WAIVER_DOCUMENT)).thenReturn(Optional.of(document));

        when(asylumCase.read(TEMP_PREVIOUS_REMISSION_DETAILS))
                .thenReturn(Optional.of(singletonList(new IdValue<>("123", mock(RemissionDetails.class)))));

        when(callback.getEvent()).thenReturn(Event.REQUEST_FEE_REMISSION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(remissionType));
        when(asylumCase.read(REMISSION_CLAIM, String.class)).thenReturn(Optional.of(remissionClaim));

        mockRemissionDecision(remissionDecision);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = requestFeeRemissionHandler.handle(ABOUT_TO_SUBMIT, callback);

        verifyTestResults(remissionType, remissionClaim, callbackResponse);
    }

    @ParameterizedTest
    @MethodSource("remissionClaimsTestData")
    void handle_should_return_new_and_previous_remission_details_help_with_fees(
        RemissionType remissionType,
        String remissionClaim,
        AppealType appealType,
        RemissionDecision remissionDecision
    ) {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        when(asylumCase.read(FEE_REMISSION_TYPE, String.class)).thenReturn(Optional.of("Help with Fees"));
        when(asylumCase.read(HELP_WITH_FEES_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("HW-A1B-123"));

        when(asylumCase.read(TEMP_PREVIOUS_REMISSION_DETAILS))
                .thenReturn(Optional.of(singletonList(new IdValue<>("123", mock(RemissionDetails.class)))));

        when(callback.getEvent()).thenReturn(Event.REQUEST_FEE_REMISSION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(remissionType));
        when(asylumCase.read(REMISSION_CLAIM, String.class)).thenReturn(Optional.of(remissionClaim));

        mockRemissionDecision(remissionDecision);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = requestFeeRemissionHandler.handle(ABOUT_TO_SUBMIT, callback);

        verifyTestResults(remissionType, remissionClaim, callbackResponse);
    }

    @ParameterizedTest
    @MethodSource("remissionClaimsTestData")
    void handle_should_return_new_and_previous_remission_details_exceptional_circumstances(
        RemissionType remissionType,
        String remissionClaim,
        AppealType appealType,
        RemissionDecision remissionDecision
    ) {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        when(asylumCase.read(FEE_REMISSION_TYPE, String.class)).thenReturn(Optional.of("Exceptional circumstances"));
        when(asylumCase.read(EXCEPTIONAL_CIRCUMSTANCES, String.class)).thenReturn(Optional.of("EC"));
        when(asylumCase.read(REMISSION_EC_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(singletonList(previousDocuments)));

        when(asylumCase.read(TEMP_PREVIOUS_REMISSION_DETAILS))
                .thenReturn(Optional.of(singletonList(new IdValue<>("123", mock(RemissionDetails.class)))));

        when(callback.getEvent()).thenReturn(Event.REQUEST_FEE_REMISSION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(remissionType));
        when(asylumCase.read(REMISSION_CLAIM, String.class)).thenReturn(Optional.of(remissionClaim));

        mockRemissionDecision(remissionDecision);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = requestFeeRemissionHandler.handle(ABOUT_TO_SUBMIT, callback);

        verifyTestResults(remissionType, remissionClaim, callbackResponse);
    }

    private void mockRemissionDecision(RemissionDecision remissionDecision) {
        switch (remissionDecision) {
            case APPROVED:
                when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(APPROVED));
                when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of("8000"));
                when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("8000"));
                when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of("0"));
                break;

            case PARTIALLY_APPROVED:
                when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(PARTIALLY_APPROVED));
                when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of("8000"));
                when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("4000"));
                when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of("4000"));
                when(asylumCase.read(REMISSION_DECISION_REASON, String.class)).thenReturn(Optional.of("A partially approved reason"));
                break;

            case REJECTED:
                when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(REJECTED));
                when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of("8000"));
                when(asylumCase.read(REMISSION_DECISION_REASON, String.class)).thenReturn(Optional.of("A rejected reason"));
                break;

            default:
                break;
        }
    }

    private void verifyTestResults(
        RemissionType remissionType,
        String remissionClaim,
        PreSubmitCallbackResponse<AsylumCase> callbackResponse
    ) {
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(2))
                .write(PREVIOUS_REMISSION_DETAILS, asylumCase.read(TEMP_PREVIOUS_REMISSION_DETAILS));
        verify(asylumCase, times(1))
                .write(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.YES);
        verify(asylumCase, times(2))
                .write(ArgumentMatchers.eq(TEMP_PREVIOUS_REMISSION_DETAILS), anyList());
        verify(asylumCase, times(1)).write(REMISSION_TYPE, remissionType);
        verify(asylumCase, times(1)).write(REMISSION_REQUESTED_BY, UserRoleLabel.ADMIN_OFFICER);
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
            Arguments.of(HO_WAIVER_REMISSION, "asylumSupport", EA, APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "asylumSupport", EA, PARTIALLY_APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "asylumSupport", EA, REJECTED),
            Arguments.of(HO_WAIVER_REMISSION, "asylumSupport", HU, APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "asylumSupport", HU, PARTIALLY_APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "asylumSupport", HU, REJECTED),
            Arguments.of(HO_WAIVER_REMISSION, "asylumSupport", PA, APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "asylumSupport", PA, PARTIALLY_APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "asylumSupport", PA, REJECTED),
            Arguments.of(HO_WAIVER_REMISSION, "asylumSupport", EU, APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "asylumSupport", EU, PARTIALLY_APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "asylumSupport", EU, REJECTED),
            Arguments.of(HO_WAIVER_REMISSION, "legalAid", EA, APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "legalAid", EA, PARTIALLY_APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "legalAid", EA, REJECTED),
            Arguments.of(HO_WAIVER_REMISSION, "legalAid", HU, APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "legalAid", HU, PARTIALLY_APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "legalAid", HU, REJECTED),
            Arguments.of(HO_WAIVER_REMISSION, "legalAid", PA, APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "legalAid", PA, PARTIALLY_APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "legalAid", PA, REJECTED),
            Arguments.of(HO_WAIVER_REMISSION, "legalAid", EU, APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "legalAid", EU, PARTIALLY_APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "legalAid", EU, REJECTED),
            Arguments.of(HO_WAIVER_REMISSION, "section17", EA, APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "section17", EA, PARTIALLY_APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "section17", EA, REJECTED),
            Arguments.of(HO_WAIVER_REMISSION, "section17", HU, APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "section17", HU, PARTIALLY_APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "section17", HU, REJECTED),
            Arguments.of(HO_WAIVER_REMISSION, "section17", PA, APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "section17", PA, PARTIALLY_APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "section17", PA, REJECTED),
            Arguments.of(HO_WAIVER_REMISSION, "section17", EU, APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "section17", EU, PARTIALLY_APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "section17", EU, REJECTED),
            Arguments.of(HO_WAIVER_REMISSION, "section20", EA, APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "section20", EA, PARTIALLY_APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "section20", EA, REJECTED),
            Arguments.of(HO_WAIVER_REMISSION, "section20", HU, APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "section20", HU, PARTIALLY_APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "section20", HU, REJECTED),
            Arguments.of(HO_WAIVER_REMISSION, "section20", PA, APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "section20", PA, PARTIALLY_APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "section20", PA, REJECTED),
            Arguments.of(HO_WAIVER_REMISSION, "section20", EU, APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "section20", EU, PARTIALLY_APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "section20", EU, REJECTED),
            Arguments.of(HO_WAIVER_REMISSION, "homeOfficeWaiver", EA, APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "homeOfficeWaiver", EA, PARTIALLY_APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "homeOfficeWaiver", EA, REJECTED),
            Arguments.of(HO_WAIVER_REMISSION, "homeOfficeWaiver", HU, APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "homeOfficeWaiver", HU, PARTIALLY_APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "homeOfficeWaiver", HU, REJECTED),
            Arguments.of(HO_WAIVER_REMISSION, "homeOfficeWaiver", PA, APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "homeOfficeWaiver", PA, PARTIALLY_APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "homeOfficeWaiver", PA, REJECTED),
            Arguments.of(HO_WAIVER_REMISSION, "homeOfficeWaiver", EU, APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "homeOfficeWaiver", EU, PARTIALLY_APPROVED),
            Arguments.of(HO_WAIVER_REMISSION, "homeOfficeWaiver", EU, REJECTED),
            Arguments.of(HELP_WITH_FEES, "Help with Fees", EA, APPROVED),
            Arguments.of(HELP_WITH_FEES, "Help with Fees", EA, PARTIALLY_APPROVED),
            Arguments.of(HELP_WITH_FEES, "Help with Fees", EA, REJECTED),
            Arguments.of(HELP_WITH_FEES, "Help with Fees", HU, APPROVED),
            Arguments.of(HELP_WITH_FEES, "Help with Fees", HU, PARTIALLY_APPROVED),
            Arguments.of(HELP_WITH_FEES, "Help with Fees", HU, REJECTED),
            Arguments.of(HELP_WITH_FEES, "Help with Fees", PA, APPROVED),
            Arguments.of(HELP_WITH_FEES, "Help with Fees", PA, PARTIALLY_APPROVED),
            Arguments.of(HELP_WITH_FEES, "Help with Fees", PA, REJECTED),
            Arguments.of(HELP_WITH_FEES, "Help with Fees", EU, APPROVED),
            Arguments.of(HELP_WITH_FEES, "Help with Fees", EU, PARTIALLY_APPROVED),
            Arguments.of(HELP_WITH_FEES, "Help with Fees", EU, REJECTED),
            Arguments.of(EXCEPTIONAL_CIRCUMSTANCES_REMISSION, "Exceptional circumstances", EA, APPROVED),
            Arguments.of(EXCEPTIONAL_CIRCUMSTANCES_REMISSION, "Exceptional circumstances", EA, PARTIALLY_APPROVED),
            Arguments.of(EXCEPTIONAL_CIRCUMSTANCES_REMISSION, "Exceptional circumstances", EA, REJECTED),
            Arguments.of(EXCEPTIONAL_CIRCUMSTANCES_REMISSION, "Exceptional circumstances", HU, APPROVED),
            Arguments.of(EXCEPTIONAL_CIRCUMSTANCES_REMISSION, "Exceptional circumstances", HU, PARTIALLY_APPROVED),
            Arguments.of(EXCEPTIONAL_CIRCUMSTANCES_REMISSION, "Exceptional circumstances", HU, REJECTED),
            Arguments.of(EXCEPTIONAL_CIRCUMSTANCES_REMISSION, "Exceptional circumstances", PA, APPROVED),
            Arguments.of(EXCEPTIONAL_CIRCUMSTANCES_REMISSION, "Exceptional circumstances", PA, PARTIALLY_APPROVED),
            Arguments.of(EXCEPTIONAL_CIRCUMSTANCES_REMISSION, "Exceptional circumstances", PA, REJECTED),
            Arguments.of(EXCEPTIONAL_CIRCUMSTANCES_REMISSION, "Exceptional circumstances", EU, APPROVED),
            Arguments.of(EXCEPTIONAL_CIRCUMSTANCES_REMISSION, "Exceptional circumstances", EU, PARTIALLY_APPROVED),
            Arguments.of(EXCEPTIONAL_CIRCUMSTANCES_REMISSION, "Exceptional circumstances", EU, REJECTED)
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

    @Test
    void should_not_append_if_aip_journey_type() {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(callback.getEvent()).thenReturn(Event.REQUEST_FEE_REMISSION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(PA));
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.CITIZEN);

        requestFeeRemissionHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, never()).write(eq(PREVIOUS_REMISSION_DETAILS), any());
        verify(asylumCase, times(1)).write(REMISSION_REQUESTED_BY, UserRoleLabel.CITIZEN);
    }
}
