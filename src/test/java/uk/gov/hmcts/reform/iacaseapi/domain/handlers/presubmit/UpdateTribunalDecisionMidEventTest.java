package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_TRIBUNAL_DECISION_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

import java.util.Optional;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UpdateTribunalDecisionMidEventTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private UserDetailsHelper userDetailsHelper;
    @Mock
    private UserDetails userDetails;
    @Mock
    private AsylumCase asylumCase;
    private UpdateTribunalDecisionMidEvent updateTribunalDecisionMidEvent;
    private String testPage = "tribunalDecisionType";

    @BeforeEach
    public void setUp() {
        updateTribunalDecisionMidEvent = new UpdateTribunalDecisionMidEvent(
            userDetailsHelper,
            userDetails
        );

        when(callback.getPageId()).thenReturn(testPage);
        when(callback.getEvent()).thenReturn(Event.UPDATE_TRIBUNAL_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, String.class))
                .thenReturn(Optional.of("underRule31"));
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getPageId()).thenReturn(testPage);

            for (PreSubmitCallbackStage callbackStage : values()) {

                boolean canHandle = updateTribunalDecisionMidEvent.canHandle(callbackStage, callback);

                if (event == Event.UPDATE_TRIBUNAL_DECISION
                    && callbackStage == PreSubmitCallbackStage.MID_EVENT
                    && callback.getPageId().equals("tribunalDecisionType")) {
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

        assertThatThrownBy(() -> updateTribunalDecisionMidEvent.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> updateTribunalDecisionMidEvent.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> updateTribunalDecisionMidEvent.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateTribunalDecisionMidEvent.canHandle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}