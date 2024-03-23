package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RemissionDetailsAppender;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.PA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ASYLUM_SUPPORT_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ASYLUM_SUPPORT_REFERENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EXCEPTIONAL_CIRCUMSTANCES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HELP_WITH_FEES_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_WAIVER_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LATE_REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_AID_ACCOUNT_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_CLAIM;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_EC_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SECTION17_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SECTION20_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.TEMP_PREVIOUS_REMISSION_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.HELP_WITH_FEES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.HO_WAIVER_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static wiremock.com.github.jknack.handlebars.helper.ConditionalHelpers.eq;

@ExtendWith(MockitoExtension.class)
class SubmitAppealHandlerTest {
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Mock private FeatureToggler featureToggler;
    private RemissionDetailsAppender remissionDetailsAppender;

    @Mock private Document document;
    @Mock private IdValue<Document> previousDocuments;

    private SubmitAppealHandler submitAppealHandler;

    @BeforeEach
    void setUp() {
        RemissionDetailsAppender remissionDetailsAppender = new RemissionDetailsAppender();

        submitAppealHandler = new SubmitAppealHandler(featureToggler, remissionDetailsAppender);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> submitAppealHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @MethodSource("remissionClaimsTestData")
    void handle_should_return_new_and_previous_remission_details_asylum_support(
            RemissionType remissionType,
            String remissionClaim,
            AppealType appealType
    ) {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        when(asylumCase.read(FEE_REMISSION_TYPE, String.class)).thenReturn(Optional.of("Asylum support"));
        when(asylumCase.read(ASYLUM_SUPPORT_REFERENCE, String.class)).thenReturn(Optional.of("123456"));
        when(asylumCase.read(ASYLUM_SUPPORT_DOCUMENT)).thenReturn(Optional.of(document));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = submitAppealHandler.handle(ABOUT_TO_SUBMIT, callback);

        verifyTestResults(callbackResponse);
    }

    @ParameterizedTest
    @MethodSource("remissionClaimsTestData")
    void handle_should_return_new_and_previous_remission_details_legal_aid(
            RemissionType remissionType,
            String remissionClaim,
            AppealType appealType
    ) {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        when(asylumCase.read(FEE_REMISSION_TYPE, String.class)).thenReturn(Optional.of("Legal Aid"));
        when(asylumCase.read(LEGAL_AID_ACCOUNT_NUMBER, String.class)).thenReturn(Optional.of("123456"));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = submitAppealHandler.handle(ABOUT_TO_SUBMIT, callback);

        verifyTestResults(callbackResponse);
    }

    @ParameterizedTest
    @MethodSource("remissionClaimsTestData")
    void handle_should_return_new_and_previous_remission_details_section_17(
            RemissionType remissionType,
            String remissionClaim,
            AppealType appealType
    ) {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        when(asylumCase.read(FEE_REMISSION_TYPE, String.class)).thenReturn(Optional.of("Section 17"));
        when(asylumCase.read(SECTION17_DOCUMENT)).thenReturn(Optional.of(document));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = submitAppealHandler.handle(ABOUT_TO_SUBMIT, callback);

        verifyTestResults(callbackResponse);
    }

    @ParameterizedTest
    @MethodSource("remissionClaimsTestData")
    void handle_should_return_new_and_previous_remission_details_section_20(
            RemissionType remissionType,
            String remissionClaim,
            AppealType appealType
    ) {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        when(asylumCase.read(FEE_REMISSION_TYPE, String.class)).thenReturn(Optional.of("Section 20"));
        when(asylumCase.read(SECTION20_DOCUMENT)).thenReturn(Optional.of(document));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = submitAppealHandler.handle(ABOUT_TO_SUBMIT, callback);

        verifyTestResults(callbackResponse);
    }

    @ParameterizedTest
    @MethodSource("remissionClaimsTestData")
    void handle_should_return_new_and_previous_remission_details_home_office_waiver(
            RemissionType remissionType,
            String remissionClaim,
            AppealType appealType
    ) {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        when(asylumCase.read(FEE_REMISSION_TYPE, String.class)).thenReturn(Optional.of("Home Office fee waiver"));
        when(asylumCase.read(HOME_OFFICE_WAIVER_DOCUMENT)).thenReturn(Optional.of(document));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = submitAppealHandler.handle(ABOUT_TO_SUBMIT, callback);

        verifyTestResults(callbackResponse);
    }

    @ParameterizedTest
    @MethodSource("remissionClaimsTestData")
    void handle_should_return_new_and_previous_remission_details_help_with_fees(
            RemissionType remissionType,
            String remissionClaim,
            AppealType appealType
    ) {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        when(asylumCase.read(FEE_REMISSION_TYPE, String.class)).thenReturn(Optional.of("Help with Fees"));
        when(asylumCase.read(HELP_WITH_FEES_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("HW-A1B-123"));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = submitAppealHandler.handle(ABOUT_TO_SUBMIT, callback);

        verifyTestResults(callbackResponse);
    }

    @ParameterizedTest
    @MethodSource("remissionClaimsTestData")
    void handle_should_return_new_and_previous_remission_details_exceptional_circumstances(
            RemissionType remissionType,
            String remissionClaim,
            AppealType appealType
    ) {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        when(asylumCase.read(FEE_REMISSION_TYPE, String.class)).thenReturn(Optional.of("Exceptional circumstances"));
        when(asylumCase.read(EXCEPTIONAL_CIRCUMSTANCES, String.class)).thenReturn(Optional.of("EC"));
        when(asylumCase.read(REMISSION_EC_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(singletonList(previousDocuments)));

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = submitAppealHandler.handle(ABOUT_TO_SUBMIT, callback);

        verifyTestResults(callbackResponse);
    }

    private void verifyTestResults(PreSubmitCallbackResponse<AsylumCase> callbackResponse) {
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
                .write(ArgumentMatchers.eq(TEMP_PREVIOUS_REMISSION_DETAILS), anyList());
    }

    private static Stream<Arguments> remissionClaimsTestData() {
        return Stream.of(
                Arguments.of(HO_WAIVER_REMISSION, "asylumSupport", EA),
                Arguments.of(HO_WAIVER_REMISSION, "asylumSupport", HU),
                Arguments.of(HO_WAIVER_REMISSION, "asylumSupport", PA),
                Arguments.of(HO_WAIVER_REMISSION, "legalAid", EA),
                Arguments.of(HO_WAIVER_REMISSION, "legalAid", HU),
                Arguments.of(HO_WAIVER_REMISSION, "legalAid", PA),
                Arguments.of(HO_WAIVER_REMISSION, "section17", EA),
                Arguments.of(HO_WAIVER_REMISSION, "section17", HU),
                Arguments.of(HO_WAIVER_REMISSION, "section17", PA),
                Arguments.of(HO_WAIVER_REMISSION, "section20", EA),
                Arguments.of(HO_WAIVER_REMISSION, "section20", HU),
                Arguments.of(HO_WAIVER_REMISSION, "section20", PA),
                Arguments.of(HO_WAIVER_REMISSION, "homeOfficeWaiver", EA),
                Arguments.of(HO_WAIVER_REMISSION, "homeOfficeWaiver", HU),
                Arguments.of(HO_WAIVER_REMISSION, "homeOfficeWaiver", PA),
                Arguments.of(HELP_WITH_FEES, "Help with Fees", EA),
                Arguments.of(HELP_WITH_FEES, "Help with Fees", HU),
                Arguments.of(HELP_WITH_FEES, "Help with Fees", PA),
                Arguments.of(EXCEPTIONAL_CIRCUMSTANCES_REMISSION, "Exceptional circumstances", EA),
                Arguments.of(EXCEPTIONAL_CIRCUMSTANCES_REMISSION, "Exceptional circumstances", HU),
                Arguments.of(EXCEPTIONAL_CIRCUMSTANCES_REMISSION, "Exceptional circumstances", PA)
        );
    }
}
