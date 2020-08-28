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

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RequestResponseReviewHandlerTest {

    private static final int DUE_IN_DAYS = 5;

    @Mock private DateProvider dateProvider;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Captor private ArgumentCaptor<String> asylumCaseValuesArgumentCaptor;
    @Captor private ArgumentCaptor<AsylumCaseFieldDefinition> asylumExtractorCaptor;

    private RequestResponseReviewHandler requestResponseReviewHandler;

    @BeforeEach
    void setUp() {

        requestResponseReviewHandler =
                new RequestResponseReviewHandler(DUE_IN_DAYS, dateProvider);
    }

    @Test
    void should_prepare_send_direction_fields() {

        final String expectedExplanationContains = "The Home Office has replied to your Appeal Skeleton Argument and evidence. You should review their response";
        final Parties expectedParties = Parties.LEGAL_REPRESENTATIVE;
        final String expectedDueDate = "2019-09-30";

        when(dateProvider.now()).thenReturn(LocalDate.parse("2019-09-25"));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONSE_REVIEW);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                requestResponseReviewHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(3)).write(asylumExtractorCaptor.capture(), asylumCaseValuesArgumentCaptor.capture());

        List<AsylumCaseFieldDefinition> extractors = asylumExtractorCaptor.getAllValues();
        List<String> asylumCaseValues = asylumCaseValuesArgumentCaptor.getAllValues();

        assertThat(
                asylumCaseValues.get(extractors.indexOf(SEND_DIRECTION_EXPLANATION)))
                .contains(expectedExplanationContains);

        assertThat(
                asylumCaseValues.get(extractors.indexOf(SEND_DIRECTION_DATE_DUE)))
                .contains(expectedDueDate);

        verify(asylumCase, times(1)).write(SEND_DIRECTION_PARTIES, expectedParties);
        verify(asylumCase, times(1)).write(SEND_DIRECTION_DATE_DUE, expectedDueDate);
    }

    @Test
    void handling_should_throw_if_cannot_actuall_handle() {
        assertThatThrownBy(() -> requestResponseReviewHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> requestResponseReviewHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = requestResponseReviewHandler.canHandle(callbackStage, callback);

                if (event == Event.REQUEST_RESPONSE_REVIEW
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arugments() {
        assertThatThrownBy(() -> requestResponseReviewHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestResponseReviewHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestResponseReviewHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestResponseReviewHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }
}
