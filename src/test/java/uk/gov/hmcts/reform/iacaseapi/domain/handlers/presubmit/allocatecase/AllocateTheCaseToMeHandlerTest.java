package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ALLOCATE_THE_CASE_TO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_WORKER_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ALLOCATE_THE_CASE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;

@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AllocateTheCaseToMeHandlerTest {

    public static final String SOME_USER_ID = "some user id";
    public static final String SOME_USER_FIRSTNAME = "some user firstname";
    public static final String SOME_USER_SURNAME = "some user surname";
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private RoleAssignmentService roleAssignmentService;
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private AllocateTheCaseService allocateTheCaseService;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AllocateTheCaseToMeHandler handler;

    @BeforeEach
    void setUp() {
        when(featureToggler.getValue("allocate-a-case-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(ALLOCATE_THE_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getId()).thenReturn(1L);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(allocateTheCaseService.isAllocateToCaseWorkerOption(any(AsylumCase.class))).thenReturn(false);
    }

    @ParameterizedTest
    @CsvSource({"false, true", "true, false"})
    void when_is_allocate_to_me_path_then_it_can_handle(boolean isAllocateToCaseWorkerOption,
                                                        boolean canHandleExpectedResult) {
        when(allocateTheCaseService.isAllocateToCaseWorkerOption(any(AsylumCase.class)))
            .thenReturn(isAllocateToCaseWorkerOption);

        boolean actual = handler.canHandle(ABOUT_TO_SUBMIT, callback);

        assertThat(actual).isEqualTo(canHandleExpectedResult);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = handler.canHandle(callbackStage, callback);

                if (event == ALLOCATE_THE_CASE
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
    void cannot_handle_if_feature_disabled() {
        when(callback.getEvent()).thenReturn(ALLOCATE_THE_CASE);
        when(featureToggler.getValue("allocate-a-case-feature", false)).thenReturn(false);

        boolean canHandle = handler.canHandle(ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }


    @Test
    void handle() {
        when(userDetails.getId()).thenReturn(SOME_USER_ID);
        when(userDetails.getForename()).thenReturn(SOME_USER_FIRSTNAME);
        when(userDetails.getSurname()).thenReturn(SOME_USER_SURNAME);

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(roleAssignmentService).assignRole(1L, SOME_USER_ID);
        verify(asylumCase).write(CASE_WORKER_NAME, String.format("%s %s", SOME_USER_FIRSTNAME, SOME_USER_SURNAME));
        verify(asylumCase).clear(eq(ALLOCATE_THE_CASE_TO));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> handler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> handler.handle(MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}