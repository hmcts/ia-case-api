package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_BEEN_REFUSED_BAIL;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DECISION_DETAILS_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IS_IMA_ENABLED;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.RECORD_DECISION_TYPE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.time.LocalDate;
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
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.DecisionType;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class IsApplicantBeenRefusedFlagHandlerTest {

    private static final int BAIL_REFUSED_WITH_IN_DAYS = 28;

    @Mock
    private DateProvider dateProvider;
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private CaseDetails<BailCase> caseDetailsBefore;
    @Mock
    private BailCase bailCaseBefore;

    private IsApplicantBeenRefusedFlagHandler isApplicantBeenRefusedFlagHandler;
    private String finalDecisionDate = "2022-06-01";

    @BeforeEach
    public void setUp() {
        isApplicantBeenRefusedFlagHandler =
            new IsApplicantBeenRefusedFlagHandler(dateProvider, BAIL_REFUSED_WITH_IN_DAYS);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(bailCaseBefore);
        when(callback.getEvent()).thenReturn(Event.MAKE_NEW_APPLICATION);
        when(bailCaseBefore.read(DECISION_DETAILS_DATE, String.class)).thenReturn(Optional.of(finalDecisionDate));
    }

    @ParameterizedTest
    @EnumSource(value = DecisionType.class, names = {"REFUSED", "REFUSED_UNDER_IMA"})
    void should_set_applicant_refused_flag_to_yes_if_with_in_days(DecisionType decisionType) {
        when(bailCaseBefore.read(RECORD_DECISION_TYPE, String.class))
            .thenReturn(Optional.of(decisionType.toString()));

        when(bailCaseBefore.read(IS_IMA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(dateProvider.now()).thenReturn(LocalDate.parse("2022-06-28"));

        PreSubmitCallbackResponse<BailCase> response = isApplicantBeenRefusedFlagHandler
            .handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);

        verify(bailCase, times(1))
            .write(APPLICANT_BEEN_REFUSED_BAIL, YesOrNo.YES);
    }

    @ParameterizedTest
    @EnumSource(value = DecisionType.class, names = {"GRANTED", "CONDITIONAL_GRANT"})
    void should_set_applicant_refused_flag_to_no_if_decision_type_not_refused(DecisionType decisionType) {

        when(bailCaseBefore.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of(decisionType.toString()));
        when(dateProvider.now()).thenReturn(LocalDate.parse("2022-06-28"));

        PreSubmitCallbackResponse<BailCase> response = isApplicantBeenRefusedFlagHandler
            .handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);

        verify(bailCase, times(1))
            .write(APPLICANT_BEEN_REFUSED_BAIL, YesOrNo.NO);
    }

    @Test
    void should_set_applicant_refused_flag_to_no_if_not_with_in_days() {
        when(bailCaseBefore.read(RECORD_DECISION_TYPE, String.class))
            .thenReturn(Optional.of(DecisionType.REFUSED.toString()));

        when(dateProvider.now()).thenReturn(LocalDate.parse("2022-06-29"));

        PreSubmitCallbackResponse<BailCase> response = isApplicantBeenRefusedFlagHandler
            .handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);

        verify(bailCase, times(1))
            .write(APPLICANT_BEEN_REFUSED_BAIL, YesOrNo.NO);
    }

    @Test
    void should_throw_when_case_details_before_is_not_present() {

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> isApplicantBeenRefusedFlagHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Case details before missing")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_for_empty_decision_date() {
        when(bailCaseBefore.read(RECORD_DECISION_TYPE, String.class))
            .thenReturn(Optional.of(DecisionType.REFUSED.toString()));
        when(bailCaseBefore.read(DECISION_DETAILS_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> isApplicantBeenRefusedFlagHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("decisionDetailsDate is not present")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);

    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = isApplicantBeenRefusedFlagHandler.canHandle(callbackStage, callback);
                if (callbackStage == ABOUT_TO_SUBMIT
                    && (callback.getEvent() == Event.MAKE_NEW_APPLICATION)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {
        //invalid stage
        assertThatThrownBy(() -> isApplicantBeenRefusedFlagHandler.handle(ABOUT_TO_START, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);

        //invalid event
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION);
        assertThatThrownBy(() -> isApplicantBeenRefusedFlagHandler.handle(ABOUT_TO_SUBMIT, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> isApplicantBeenRefusedFlagHandler.canHandle(null, callback)).hasMessage(
            "callbackStage must not be null").isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> isApplicantBeenRefusedFlagHandler.canHandle(ABOUT_TO_SUBMIT, null)).hasMessage(
            "callback must not be null").isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> isApplicantBeenRefusedFlagHandler.handle(null, callback)).hasMessage(
            "callbackStage must not be null").isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> isApplicantBeenRefusedFlagHandler.handle(ABOUT_TO_SUBMIT, null)).hasMessage(
            "callback must not be null").isExactlyInstanceOf(NullPointerException.class);
    }
}
