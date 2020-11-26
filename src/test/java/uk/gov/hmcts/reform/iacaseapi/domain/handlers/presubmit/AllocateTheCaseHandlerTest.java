package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ALLOCATE_THE_CASE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.RoleAssignmentService;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AllocateTheCaseHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Mock private RoleAssignmentService roleAssignmentService;
    @Mock private FeatureToggler featureToggler;

    private AllocateTheCaseHandler allocateTheCaseHandler;

    @Before
    public void createHandler() {
        allocateTheCaseHandler = new AllocateTheCaseHandler(roleAssignmentService, featureToggler);
        when(featureToggler.getValue("allocate-a-case-feature", false)).thenReturn(true);
    }

    @Test
    public void assigns_role() {
        when(callback.getEvent()).thenReturn(ALLOCATE_THE_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        allocateTheCaseHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(roleAssignmentService).assignRole(caseDetails);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> allocateTheCaseHandler.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> allocateTheCaseHandler.handle(MID_EVENT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = allocateTheCaseHandler.canHandle(callbackStage, callback);

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
    public void cannot_handle_if_feature_disabled() {
        when(callback.getEvent()).thenReturn(ALLOCATE_THE_CASE);
        when(featureToggler.getValue("allocate-a-case-feature", false)).thenReturn(false);
        boolean canHandle = allocateTheCaseHandler.canHandle(ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> allocateTheCaseHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> allocateTheCaseHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> allocateTheCaseHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> allocateTheCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }
}
