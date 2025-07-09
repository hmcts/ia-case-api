package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.PA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DueDateService;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class RequestRespondentReviewPreparerTest {

    private static final int DUE_IN_DAYS = 14;
    private static final int ADA_DUE_IN_DAYS = 2;
    private static final int DETAINED_DUE_IN_DAYS = 7;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Mock
    private DueDateService dueDateService;

    @Captor
    private ArgumentCaptor<YesOrNo> asylumYesNoCaptor;
    @Captor
    private ArgumentCaptor<String> asylumValueCaptor;
    @Captor
    private ArgumentCaptor<AsylumCaseFieldDefinition> asylumExtractorCaptor;

    private RequestRespondentReviewPreparer requestRespondentReviewPreparer;

    @BeforeEach
    public void setUp() {
        requestRespondentReviewPreparer =
            new RequestRespondentReviewPreparer(DUE_IN_DAYS, ADA_DUE_IN_DAYS, DETAINED_DUE_IN_DAYS, dateProvider, dueDateService);
    }

    @ParameterizedTest
    @ValueSource (strings = {"", "YES", "NO"})
    void should_prepare_send_direction_fields(String isAda) {

        final String expectedExplanationContains = "By the date below you must review the appellant’s ASA and bundle.";
        final Parties expectedParties = Parties.RESPONDENT;
        final String expectedDateDue = "2018-12-07";
        final String expectedAdaDateDue = "2018-11-25";

        when(dateProvider.now()).thenReturn(LocalDate.parse("2018-11-23"));
        when(dueDateService.calculateDueDate(any(), eq(ADA_DUE_IN_DAYS))).thenReturn(LocalDate.parse(expectedAdaDateDue).atStartOfDay(ZoneOffset.UTC));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONDENT_REVIEW);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class))
                .thenReturn(isAda.isEmpty() ? Optional.empty() : Optional.of(YesOrNo.valueOf(isAda)));

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

        assertThat(
            asylumCaseValues.get(extractors.indexOf(SEND_DIRECTION_EXPLANATION)))
            .containsSequence(expectedExplanationContains);

        assertThat(
            asylumYesNoValues.get(extractors.indexOf(UPLOAD_HOME_OFFICE_APPEAL_RESPONSE_ACTION_AVAILABLE)))
            .isEqualByComparingTo(YesOrNo.YES);

        verify(asylumCase, times(1)).write(SEND_DIRECTION_PARTIES, expectedParties);
        verify(asylumCase, times(1)).write(SEND_DIRECTION_DATE_DUE, isAda.equals("YES")
                ? expectedAdaDateDue : expectedDateDue);
    }

    @Test
    void should_prepare_send_direction_fields_detained() {

        final String expectedExplanationContains = "The review must comply with (i) Rule 24A (3) of the Tribunal Procedure Rules 2014 and (ii) the Practice Direction Part 2, section 2.1 (e), Part 3.";
        final Parties expectedParties = Parties.RESPONDENT;
        // detained appeals due date 7 calendar days in the future
        final String expectedDateDue = "2018-11-30";

        when(dateProvider.now()).thenReturn(LocalDate.parse("2018-11-23"));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONDENT_REVIEW);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.ofNullable(YesOrNo.YES));

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

        assertThat(
                asylumCaseValues.get(extractors.indexOf(SEND_DIRECTION_EXPLANATION)))
                .containsSequence(expectedExplanationContains);

        assertThat(
                asylumYesNoValues.get(extractors.indexOf(UPLOAD_HOME_OFFICE_APPEAL_RESPONSE_ACTION_AVAILABLE)))
                .isEqualByComparingTo(YesOrNo.YES);

        verify(asylumCase, times(1)).write(SEND_DIRECTION_PARTIES, expectedParties);
        verify(asylumCase, times(1)).write(SEND_DIRECTION_DATE_DUE, expectedDateDue);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> requestRespondentReviewPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(
            () -> requestRespondentReviewPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
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

    @Test
    void should_return_due_date_for_accelerated_detained_appeal() {
        LocalDate today = LocalDate.of(2025, 5,5);
        ZonedDateTime adaDueDate = today.plusDays(ADA_DUE_IN_DAYS).atStartOfDay(ZoneOffset.UTC);
        String expectedDueDate = "2025-05-07";

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONDENT_REVIEW);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(dateProvider.now()).thenReturn(today);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(PA));
        when(dueDateService.calculateDueDate(
                any(), anyInt()))
                .thenReturn(adaDueDate);

        PreSubmitCallbackResponse<AsylumCase> response =
                requestRespondentReviewPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(SEND_DIRECTION_DATE_DUE, expectedDueDate);
    }

    @Test
    void should_return_due_date_for_standard_case() {
        LocalDate today = LocalDate.of(2025, 5,5);
        String expectedDueDate = "2025-05-19";

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONDENT_REVIEW);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(dateProvider.now()).thenReturn(today);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(PA));

        PreSubmitCallbackResponse<AsylumCase> response =
                requestRespondentReviewPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(SEND_DIRECTION_DATE_DUE, expectedDueDate);
    }

    @Test
    void should_return_due_date_for_detained_case() {
        LocalDate today = LocalDate.of(2025, 5,5);
        String expectedDueDate = "2025-05-12";

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONDENT_REVIEW);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(dateProvider.now()).thenReturn(today);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(PA));

        PreSubmitCallbackResponse<AsylumCase> response =
                requestRespondentReviewPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(SEND_DIRECTION_DATE_DUE, expectedDueDate);
    }

}
