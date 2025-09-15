package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.values;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class HomeOfficeDecisionDateValidatorTest {

    private static final String HOME_OFFICE_DECISION_LETTER_PAGE_ID = "homeOfficeDecisionLetter";
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    private String today;
    private String tomorrow;
    private String callbackErrorMessageUkAg = "Date of decision letter must not be in the future.";
    private String callbackErrorMessageUkNonAg = "Home Office decision date must not be in the future.";
    private String callbackErrorMessageNonUk = "Decision letter received date must not be in the future.";
    private HomeOfficeDecisionDateValidator homeOfficeDecisionDateValidator;

    @BeforeEach
    public void setUp() {
        homeOfficeDecisionDateValidator = new HomeOfficeDecisionDateValidator();

        LocalDate now = LocalDate.now();
        today = now.toString();
        tomorrow = now.plusDays(1).toString();

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getPageId()).thenReturn(HOME_OFFICE_DECISION_LETTER_PAGE_ID);
    }

    @ParameterizedTest
    @ValueSource(strings = { HOME_OFFICE_DECISION_LETTER_PAGE_ID, ""})

    void it_can_handle_callback(String pageId) {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            when(callback.getPageId()).thenReturn(pageId);

            for (PreSubmitCallbackStage callbackStage : values()) {
                boolean canHandle = homeOfficeDecisionDateValidator.canHandle(callbackStage, callback);

                if ((event == Event.START_APPEAL || event == Event.EDIT_APPEAL)
                    && callbackStage == MID_EVENT
                    && callback.getPageId().equals(HOME_OFFICE_DECISION_LETTER_PAGE_ID)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> homeOfficeDecisionDateValidator.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> homeOfficeDecisionDateValidator.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> homeOfficeDecisionDateValidator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeDecisionDateValidator.canHandle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_error_when_date_is_future_uk_ag() {
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.AG));
        when(asylumCase.read(AsylumCaseFieldDefinition.DATE_ON_DECISION_LETTER, String.class))
            .thenReturn(Optional.of(tomorrow));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeDecisionDateValidator.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1).containsOnly(callbackErrorMessageUkAg);
    }

    @Test
    void should_error_when_date_is_future_uk_non_ag() {
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(AsylumCaseFieldDefinition.HOME_OFFICE_DECISION_DATE, String.class))
            .thenReturn(Optional.of(tomorrow));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeDecisionDateValidator.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1).containsOnly(callbackErrorMessageUkNonAg);
    }

    @Test
    void should_error_when_date_is_future_non_uk() {
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(AsylumCaseFieldDefinition.DECISION_LETTER_RECEIVED_DATE, String.class))
            .thenReturn(Optional.of(tomorrow));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeDecisionDateValidator.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1).containsOnly(callbackErrorMessageNonUk);
    }

    @Test
    void should_not_error_when_date_is_not_future_uk_ag() {
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.AG));
        when(asylumCase.read(AsylumCaseFieldDefinition.DATE_ON_DECISION_LETTER, String.class))
            .thenReturn(Optional.of(today));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeDecisionDateValidator.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    void should_not_error_when_date_is_not_future_uk_non_ag() {
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(AsylumCaseFieldDefinition.HOME_OFFICE_DECISION_DATE, String.class))
            .thenReturn(Optional.of(today));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeDecisionDateValidator.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    void should_not_error_when_date_is_not_future_non_uk() {
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(AsylumCaseFieldDefinition.DECISION_LETTER_RECEIVED_DATE, String.class))
            .thenReturn(Optional.of(today));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeDecisionDateValidator.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    void should_error_when_date_is_not_there_uk_ag() {
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.AG));
        when(asylumCase.read(AsylumCaseFieldDefinition.DATE_ON_DECISION_LETTER, String.class))
            .thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> homeOfficeDecisionDateValidator.handle(MID_EVENT, callback))
            .hasMessage("Date of decision letter missing")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);    
    }

    @Test
    void should_error_when_date_is_not_there_uk_non_ag() {
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(AsylumCaseFieldDefinition.HOME_OFFICE_DECISION_DATE, String.class))
            .thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> homeOfficeDecisionDateValidator.handle(MID_EVENT, callback))
            .hasMessage("Home Office decision date missing")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);    
    }

    @Test
    void should_error_when_date_is_not_there_non_uk() {
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(AsylumCaseFieldDefinition.DECISION_LETTER_RECEIVED_DATE, String.class))
            .thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> homeOfficeDecisionDateValidator.handle(MID_EVENT, callback))
            .hasMessage("Decision letter received date missing")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);    
    }
}
