package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CASE_MANAGEMENT_CATEGORY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@ExtendWith(MockitoExtension.class)
class CaseManagementCategoryAppenderTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;

    private CaseManagementCategoryAppender caseManagementCategoryAppender;

    @BeforeEach
    public void setUp() {
        caseManagementCategoryAppender = new CaseManagementCategoryAppender();
    }

    @Test
    void handler_sets_case_management_category_value() {

        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);

        PreSubmitCallbackResponse<BailCase> response =
            caseManagementCategoryAppender.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertThat(response.getErrors()).isEmpty();
        Value value = new Value("bail", "Bail");
        DynamicList dynamicList = new DynamicList(value, Collections.singletonList(value));
        Mockito.verify(bailCase, times(1)).write(CASE_MANAGEMENT_CATEGORY, dynamicList);
    }

    @ParameterizedTest
    @EnumSource(
        value = Event.class,
        names = {"START_APPLICATION", "MAKE_NEW_APPLICATION"}
    )
    void canHandle_returns_true_for_supported_events(Event event) {
        when(callback.getEvent()).thenReturn(event);

        boolean canHandle = caseManagementCategoryAppender.canHandle(ABOUT_TO_SUBMIT, callback);

        assertTrue(canHandle);
    }

    @ParameterizedTest
    @EnumSource(
        value = Event.class,
        mode = EnumSource.Mode.EXCLUDE,
        names = {"START_APPLICATION", "MAKE_NEW_APPLICATION"}
    )
    void canHandle_returns_false_for_unsupported_events(Event event) {
        when(callback.getEvent()).thenReturn(event);

        boolean canHandle = caseManagementCategoryAppender.canHandle(ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> caseManagementCategoryAppender.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseManagementCategoryAppender.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseManagementCategoryAppender.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseManagementCategoryAppender.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {
        assertThatThrownBy(() -> caseManagementCategoryAppender.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
