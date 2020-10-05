package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class RequestNewHearingRequirementsDirectionPreparerTest {

    @Mock private DateProvider dateProvider;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private FeatureToggler featureToggler;

    private static final int HEARING_REQUIREMENTS_DUE_IN_DAYS = 5;
    private RequestNewHearingRequirementsDirectionPreparer requestNewHearingRequirementsDirectionPreparer;

    @Before
    public void setUp() {
        requestNewHearingRequirementsDirectionPreparer =
            new RequestNewHearingRequirementsDirectionPreparer(
                HEARING_REQUIREMENTS_DUE_IN_DAYS,
                dateProvider,
                featureToggler
            );
    }

    @Test
    public void can_handle_request_hearing_requirements_feature() {

        when(callback.getEvent()).thenReturn(Event.REQUEST_NEW_HEARING_REQUIREMENTS);
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);

        requestNewHearingRequirementsDirectionPreparer =
            new RequestNewHearingRequirementsDirectionPreparer(
                HEARING_REQUIREMENTS_DUE_IN_DAYS,
                dateProvider,
                featureToggler
            );

        boolean canHandle = requestNewHearingRequirementsDirectionPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertTrue(canHandle);
    }

    @Test
    public void should_prepare_send_direction_fields() {

        final String expectedExplanationContains = "This appeal will be reheard. You should tell the Tribunal if the appellant’s hearing requirements have changed.\n\n"
                                                   + "# Next steps\n\n"
                                                   + "Visit the online service and use the HMCTS reference to find the case. Use the link on the overview tab to submit the appellant’s hearing requirements.\n"
                                                   + "The Tribunal will review the hearing requirements and any requests for additional adjustments. You'll then be sent a hearing date.\n"
                                                   + "If you do not submit the hearing requirements within 5 working days, the Tribunal may not be able to accommodate the appellant's needs for the hearing.";

        final Parties expectedParties = Parties.LEGAL_REPRESENTATIVE;
        final String expectedDueDate = "2020-10-06";

        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-10-01"));
        when(callback.getEvent()).thenReturn(Event.REQUEST_NEW_HEARING_REQUIREMENTS);
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            requestNewHearingRequirementsDirectionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        assertThat(callbackResponse.getErrors()).isEmpty();

        verify(asylumCase, times(1)).write(SEND_DIRECTION_EXPLANATION, expectedExplanationContains);
        verify(asylumCase, times(1)).write(SEND_DIRECTION_PARTIES, expectedParties);
        verify(asylumCase, times(1)).write(SEND_DIRECTION_DATE_DUE, expectedDueDate);
    }

    @Test
    public void should_display_error_when_not_a_reheard_decision() {

        when(callback.getEvent()).thenReturn(Event.REQUEST_NEW_HEARING_REQUIREMENTS);
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            requestNewHearingRequirementsDirectionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).contains("You cannot request hearing requirements for this appeal in this state.");
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> requestNewHearingRequirementsDirectionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> requestNewHearingRequirementsDirectionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = requestNewHearingRequirementsDirectionPreparer.canHandle(callbackStage, callback);

                if (event == Event.REQUEST_NEW_HEARING_REQUIREMENTS
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(featureToggler.getValue("reheard-feature", false)).thenReturn(false);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = requestNewHearingRequirementsDirectionPreparer.canHandle(callbackStage, callback);

                if (event == Event.REQUEST_NEW_HEARING_REQUIREMENTS
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> requestNewHearingRequirementsDirectionPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestNewHearingRequirementsDirectionPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestNewHearingRequirementsDirectionPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestNewHearingRequirementsDirectionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
