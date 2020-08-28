package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RequestRespondentReviewPreparerTest {

    private static final int DUE_IN_DAYS = 14;

    @Mock private DateProvider dateProvider;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Captor ArgumentCaptor<YesOrNo> asylumYesNoCaptor;
    @Captor ArgumentCaptor<String> asylumValueCaptor;
    @Captor ArgumentCaptor<AsylumCaseFieldDefinition> asylumExtractorCaptor;

    private RequestRespondentReviewPreparer requestRespondentReviewPreparer;

    @BeforeEach
    void setUp() {

        requestRespondentReviewPreparer =
            new RequestRespondentReviewPreparer(DUE_IN_DAYS, dateProvider);
    }

    @Test
    void should_prepare_send_direction_fields() {

        final String expectedExplanationContains = "You must respond to the Tribunal and tell them:";
        final Parties expectedParties = Parties.RESPONDENT;
        final String expectedDateDue = "2018-12-07";

        when(dateProvider.now()).thenReturn(LocalDate.parse("2018-11-23"));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONDENT_REVIEW);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            requestRespondentReviewPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(4)).write(
                asylumExtractorCaptor.capture(),
                asylumValueCaptor.capture());

        verify(asylumCase, times(4)).write(
                asylumExtractorCaptor.capture(),
                asylumYesNoCaptor.capture());

        List<AsylumCaseFieldDefinition> extractors = asylumExtractorCaptor.getAllValues();
        List<String> asylumCaseValues = asylumValueCaptor.getAllValues();
        List<YesOrNo> asylumYesNoValues = asylumYesNoCaptor.getAllValues();

        assertThat(asylumCaseValues.get(extractors.indexOf(SEND_DIRECTION_EXPLANATION)))
            .contains("You have " + DUE_IN_DAYS + " days");


        assertThat(
                asylumCaseValues.get(extractors.indexOf(SEND_DIRECTION_EXPLANATION)))
            .contains(expectedExplanationContains);

        assertSame(
                asylumYesNoValues.get(extractors.indexOf(UPLOAD_HOME_OFFICE_APPEAL_RESPONSE_ACTION_AVAILABLE)),
            YesOrNo.YES);

        verify(asylumCase, times(1)).write(SEND_DIRECTION_PARTIES, expectedParties);
        verify(asylumCase, times(1)).write(SEND_DIRECTION_DATE_DUE, expectedDateDue);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> requestRespondentReviewPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> requestRespondentReviewPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = requestRespondentReviewPreparer.canHandle(callbackStage, callback);

                if (event == Event.REQUEST_RESPONDENT_REVIEW
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> requestRespondentReviewPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestRespondentReviewPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestRespondentReviewPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestRespondentReviewPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
