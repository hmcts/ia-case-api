package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
import uk.gov.hmcts.reform.iacaseapi.domain.service.DueDateService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class RequestCaseBuildingPreparerTest {

    private static final int DUE_IN_DAYS = 28;
    private static final int DUE_IN_DAYS_ADA = 9;
    private static final int DUE_IN_DAYS_FROM_SUBMISSION_DATE = 42;

    @Mock
    private DateProvider dateProvider;
    @Mock
    private DueDateService dueDateService;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Captor
    private ArgumentCaptor<String> asylumCaseValuesArgumentCaptor;
    @Captor
    private ArgumentCaptor<AsylumCaseFieldDefinition> asylumExtractorCaptor;

    private RequestCaseBuildingPreparer requestCaseBuildingPreparer;

    @BeforeEach
    public void setUp() {
        requestCaseBuildingPreparer =
            new RequestCaseBuildingPreparer(DUE_IN_DAYS, DUE_IN_DAYS_FROM_SUBMISSION_DATE, DUE_IN_DAYS_ADA, dueDateService, dateProvider);
    }

    @ParameterizedTest
    @MethodSource("caseTypeScenarios")
    void should_prepare_send_direction_fields(YesOrNo yesOrNo, Parties expectedParties) {
        String expectedExplanationContains;
        if (yesOrNo == YES) {
            // match detained direction text
            expectedExplanationContains = "The form and content of this ASA must comply with the terms of Practice Direction";
        } else {
            // match non-detained direction text
            expectedExplanationContains = "The appellant and their representative are reminded that they have an obligation under Rule 2(4)";
        }

        final String expectedDueDate = "2019-10-08";

        when(dateProvider.now()).thenReturn(LocalDate.parse("2019-09-10"));
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.of("2019-08-10"));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_CASE_BUILDING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.ofNullable(yesOrNo));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.ofNullable(yesOrNo));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            requestCaseBuildingPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);


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

    @ParameterizedTest
    @MethodSource("caseTypeScenariosEjp")
    void should_prepare_send_direction_fields_ejp(
        YesOrNo isEjp,
        YesOrNo appellantInDetention,
        YesOrNo isLegallyRepresentedEjp,
        Parties party
    ) {
        String expectedExplanationContains;
        if (appellantInDetention == YES) {
            // match detained direction text
            expectedExplanationContains = "The form and content of this ASA must comply with the terms of Practice Direction";
        } else {
            // match non-detained direction text
            expectedExplanationContains = "The appellant and their representative are reminded that they have an obligation under Rule 2(4)";
        }

        final String expectedDueDate = "2019-10-08";

        when(dateProvider.now()).thenReturn(LocalDate.parse("2019-09-10"));
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.of("2019-08-10"));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_CASE_BUILDING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_EJP, YesOrNo.class)).thenReturn(Optional.ofNullable(isEjp));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.ofNullable(appellantInDetention));
        when(asylumCase.read(IS_LEGALLY_REPRESENTED_EJP, YesOrNo.class)).thenReturn(Optional.ofNullable(isLegallyRepresentedEjp));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            requestCaseBuildingPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

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

        verify(asylumCase, times(1)).write(SEND_DIRECTION_PARTIES, party);
        verify(asylumCase, times(1)).write(SEND_DIRECTION_DATE_DUE, expectedDueDate);
    }

    @Test
    void handling_should_throw_if_cannot_actuall_handle() {
        assertThatThrownBy(() -> requestCaseBuildingPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> requestCaseBuildingPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = requestCaseBuildingPreparer.canHandle(callbackStage, callback);

                if ((event == Event.REQUEST_CASE_BUILDING || event == Event.FORCE_REQUEST_CASE_BUILDING)
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
        assertThatThrownBy(() -> requestCaseBuildingPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestCaseBuildingPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestCaseBuildingPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestCaseBuildingPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_return_submission_date_plus_42_days_when_submission_date_is_2_days_ago() {

        LocalDate submissionDateWithin42Days = LocalDate.now().minusDays(2);

        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class))
            .thenReturn(Optional.of(submissionDateWithin42Days.toString()));

        when(dateProvider.now()).thenReturn(LocalDate.now());

        final LocalDate buildCaseDirectionDueDate = RequestCaseBuildingPreparer
            .getBuildCaseDirectionDueDate(asylumCase, dateProvider, DUE_IN_DAYS_FROM_SUBMISSION_DATE, DUE_IN_DAYS);

        assertThat(buildCaseDirectionDueDate)
            .isEqualTo(submissionDateWithin42Days.plusDays(DUE_IN_DAYS_FROM_SUBMISSION_DATE));
    }

    @Test
    void should_return_current_date_plus_28_days_when_submission_date_is_20_days_ago() {

        LocalDate submissionDateWithin42Days = LocalDate.now().minusDays(20);

        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class))
            .thenReturn(Optional.of(submissionDateWithin42Days.toString()));

        when(dateProvider.now()).thenReturn(LocalDate.now());

        final LocalDate buildCaseDirectionDueDate = RequestCaseBuildingPreparer
            .getBuildCaseDirectionDueDate(asylumCase, dateProvider, DUE_IN_DAYS_FROM_SUBMISSION_DATE, DUE_IN_DAYS);

        assertThat(buildCaseDirectionDueDate).isEqualTo(LocalDate.now().plusDays(DUE_IN_DAYS));
    }

    @Test
    void should_return_current_date_plus_9_days_when_submission_is_an_ada_case() {

        final String expectedExplanationContains =
                "The appellant and their representative are reminded that they have an obligation under Rule 2(4)";
        final Parties expectedParties = Parties.LEGAL_REPRESENTATIVE;
        final String expectedDueDate = "2023-02-16";
        final ZonedDateTime zonedDueDateTime = LocalDate.parse(expectedDueDate).atStartOfDay(ZoneOffset.UTC);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.REQUEST_CASE_BUILDING);
        when(asylumCase.read(AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));

        when(dueDateService.calculateDueDate(any(), eq(DUE_IN_DAYS_ADA))).thenReturn(zonedDueDateTime);

        requestCaseBuildingPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

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

    static Stream<Arguments> caseTypeScenarios() {
        return Stream.of(
                Arguments.of(YES, Parties.APPELLANT),
                Arguments.of(NO, Parties.LEGAL_REPRESENTATIVE)
        );
    }

    static Stream<Arguments> caseTypeScenariosEjp() {
        return Stream.of(
            Arguments.of(YES, NO, NO, Parties.APPELLANT),
            Arguments.of(NO, NO, NO, Parties.LEGAL_REPRESENTATIVE)
        );
    }

}
