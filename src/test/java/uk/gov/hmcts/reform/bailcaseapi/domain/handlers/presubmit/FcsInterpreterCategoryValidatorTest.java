package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class FcsInterpreterCategoryValidatorTest {
    @Mock private Callback<BailCase> callback;
    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;
    private FcsInterpreterCategoryValidator fcsInterpreterCategoryValidator;

    @BeforeEach
    void setUp() {
        reset(callback);
        fcsInterpreterCategoryValidator = new FcsInterpreterCategoryValidator();
        when(callback.getEvent()).thenReturn(Event.EDIT_BAIL_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getPageId()).thenReturn("fcsInterpreterCategory");
    }


    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> fcsInterpreterCategoryValidator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);
        assertThatThrownBy(() -> fcsInterpreterCategoryValidator.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            when(callback.getPageId()).thenReturn("fcsInterpreterCategory");
            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = fcsInterpreterCategoryValidator.canHandle(callbackStage, callback);
                assertThat(canHandle).isEqualTo(
                    callbackStage == PreSubmitCallbackStage.MID_EVENT
                        && (event.equals(Event.EDIT_BAIL_APPLICATION)
                            || event.equals(Event.MAKE_NEW_APPLICATION)
                            || event.equals(Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT)
                            || event.equals(Event.START_APPLICATION)));
            }
            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> fcsInterpreterCategoryValidator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> fcsInterpreterCategoryValidator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> fcsInterpreterCategoryValidator.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> fcsInterpreterCategoryValidator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_throw_error_if_4fcs_present_no_category_is_selected() {
        when(bailCase.read(BailCaseFieldDefinition.FCS_INTERPRETER_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.FCS1_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(Collections.EMPTY_LIST));
        when(bailCase.read(BailCaseFieldDefinition.FCS2_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(Collections.EMPTY_LIST));
        when(bailCase.read(BailCaseFieldDefinition.FCS3_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(Collections.EMPTY_LIST));
        when(bailCase.read(BailCaseFieldDefinition.FCS4_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(Collections.EMPTY_LIST));
        when(bailCase.read(BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_4, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<BailCase> response = fcsInterpreterCategoryValidator.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertTrue(response.getErrors().size() == 1);
        assertTrue(response.getErrors().contains("You must select at least one interpreter category"));
    }

    @Test
    void should_not_throw_error_if_4fcs_present_one_category_is_selected() {
        when(bailCase.read(BailCaseFieldDefinition.FCS_INTERPRETER_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.FCS1_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(Collections.EMPTY_LIST));
        when(bailCase.read(BailCaseFieldDefinition.FCS2_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(Collections.EMPTY_LIST));
        when(bailCase.read(BailCaseFieldDefinition.FCS3_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(List.of("signLanguage")));
        when(bailCase.read(BailCaseFieldDefinition.FCS4_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(Collections.EMPTY_LIST));
        when(bailCase.read(BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_4, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<BailCase> response = fcsInterpreterCategoryValidator.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertTrue(response.getErrors().size() == 0);
    }

    @Test
    void should_throw_error_if_2fcs_present_no_category_selected() {
        when(bailCase.read(BailCaseFieldDefinition.FCS_INTERPRETER_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.FCS1_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(Collections.EMPTY_LIST));
        when(bailCase.read(BailCaseFieldDefinition.FCS2_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(Collections.EMPTY_LIST));
        when(bailCase.read(BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<BailCase> response = fcsInterpreterCategoryValidator.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertTrue(response.getErrors().size() == 1);
        assertTrue(response.getErrors().contains("You must select at least one interpreter category"));
    }

    @Test
    void should_throw_error_if_3fcs_present_no_category_selected() {
        when(bailCase.read(BailCaseFieldDefinition.FCS_INTERPRETER_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.FCS1_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(Collections.EMPTY_LIST));
        when(bailCase.read(BailCaseFieldDefinition.FCS2_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(Collections.EMPTY_LIST));
        when(bailCase.read(BailCaseFieldDefinition.FCS3_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(Collections.EMPTY_LIST));
        when(bailCase.read(BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<BailCase> response = fcsInterpreterCategoryValidator.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertTrue(response.getErrors().size() == 1);
        assertTrue(response.getErrors().contains("You must select at least one interpreter category"));
    }

    @Test
    void should_throw_error_if_1fcs_present_no_category_selected() {
        when(bailCase.read(BailCaseFieldDefinition.FCS_INTERPRETER_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.FCS1_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(Collections.EMPTY_LIST));
        when(bailCase.read(BailCaseFieldDefinition.FCS2_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(Collections.EMPTY_LIST));
        when(bailCase.read(BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<BailCase> response = fcsInterpreterCategoryValidator.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertTrue(response.getErrors().size() == 1);
        assertTrue(response.getErrors().contains("You must select at least one interpreter category"));
    }

    @Test
    void should_handle_if_no_fcs_present_no_category_selected() {
        when(bailCase.read(BailCaseFieldDefinition.FCS_INTERPRETER_YESNO, YesOrNo.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<BailCase> response = fcsInterpreterCategoryValidator.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertTrue(response.getErrors().size() == 0);
    }

    @Test
    void should_throw_error_if_4fcs_present_previously_but_now_only_fcs1_exist_and_no_category_is_selected() {
        when(bailCase.read(BailCaseFieldDefinition.FCS_INTERPRETER_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.FCS1_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(Collections.EMPTY_LIST));
        when(bailCase.read(BailCaseFieldDefinition.FCS2_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(List.of("spokenLanguage", "signLanguage")));
        when(bailCase.read(BailCaseFieldDefinition.FCS3_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(List.of("signLanguage")));
        when(bailCase.read(BailCaseFieldDefinition.FCS4_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(List.of("spokenLanguage")));
        when(bailCase.read(BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_4, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<BailCase> response = fcsInterpreterCategoryValidator.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertTrue(response.getErrors().size() == 1);
        assertTrue(response.getErrors().contains("You must select at least one interpreter category"));
    }

}
