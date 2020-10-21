package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class DisableAllocateTheCaseHandlerTest {
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Mock private FeatureToggler featureToggler;

    private DisableAllocateTheCaseHandler disableAllocateTheCaseHandler;

    @Before
    public void createHandler() {
        disableAllocateTheCaseHandler = new DisableAllocateTheCaseHandler(featureToggler);
        when(featureToggler.getValue("allocate-a-case-feature", false)).thenReturn(false);
    }

    @Test
    public void adds_error_handled() {
        when(callback.getEvent()).thenReturn(ALLOCATE_THE_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> handle = disableAllocateTheCaseHandler.handle(ABOUT_TO_START, callback);

        assertEquals(asSet("Allocate case feature had been disabled"), handle.getErrors());
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> disableAllocateTheCaseHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> disableAllocateTheCaseHandler.handle(MID_EVENT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = disableAllocateTheCaseHandler.canHandle(callbackStage, callback);

                if (event == ALLOCATE_THE_CASE
                        && callbackStage == ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void cannot_handle_if_feature_Enabled() {
        when(callback.getEvent()).thenReturn(ALLOCATE_THE_CASE);
        when(featureToggler.getValue("allocate-a-case-feature", false)).thenReturn(true);
        boolean canHandle = disableAllocateTheCaseHandler.canHandle(ABOUT_TO_START, callback);

        assertFalse(canHandle);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> disableAllocateTheCaseHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> disableAllocateTheCaseHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> disableAllocateTheCaseHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> disableAllocateTheCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }
}
