package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType.REP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import java.util.ArrayList;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RequestHearingRequirementsDirectionTest {

    private static final int HEARING_REQUIREMENTS_DUE_IN_DAYS = 5;

    @Mock
    private DateProvider dateProvider;
    @Mock
    private DirectionAppender directionAppender;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Captor
    private ArgumentCaptor<List<IdValue<Direction>>> existingDirectionsCaptor;

    private RequestHearingRequirementsDirectionHandler requestHearingRequirementsDirectionHandler;

    @BeforeEach
    public void setUp() {
        requestHearingRequirementsDirectionHandler =
            new RequestHearingRequirementsDirectionHandler(
                HEARING_REQUIREMENTS_DUE_IN_DAYS,
                dateProvider,
                directionAppender
            );
    }

    @Test
    void can_handle_request_hearing_requirements_feature() {

        when(callback.getEvent()).thenReturn(Event.REQUEST_HEARING_REQUIREMENTS_FEATURE);

        requestHearingRequirementsDirectionHandler =
            new RequestHearingRequirementsDirectionHandler(
                HEARING_REQUIREMENTS_DUE_IN_DAYS,
                dateProvider,
                directionAppender
            );

        boolean canHandle =
            requestHearingRequirementsDirectionHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertTrue(canHandle);
    }

    @ParameterizedTest
    @MethodSource("caseTypeScenarios")
    void should_append_new_direction_to_existing_directions_for_the_case(YesOrNo yesOrNo, YesOrNo ada, JourneyType journeyType, Parties party) {

        final List<IdValue<Direction>> existingDirections = new ArrayList<>();
        final List<IdValue<Direction>> allDirections = new ArrayList<>();

        final String expectedExplanationPart =
            "Visit the online service and use the HMCTS reference to find the case. You'll be able to submit the hearing requirements by following the instructions on the overview tab.";
        final String expectedDateDue = "2018-12-25";
        final DirectionTag expectedTag = DirectionTag.LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS;

        when(dateProvider.now()).thenReturn(LocalDate.parse("2018-12-20"));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_HEARING_REQUIREMENTS_FEATURE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(existingDirections));

        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.ofNullable(yesOrNo));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.ofNullable(yesOrNo));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.ofNullable(ada));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(journeyType));

        when(directionAppender.append(
            eq(asylumCase),
            eq(existingDirections),
            contains(expectedExplanationPart),
            eq(party),
            eq(expectedDateDue),
            eq(expectedTag),
            eq(Event.REQUEST_HEARING_REQUIREMENTS_FEATURE.toString())
        )).thenReturn(allDirections);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            requestHearingRequirementsDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(directionAppender, times(1)).append(
            eq(asylumCase),
            eq(existingDirections),
            contains(expectedExplanationPart),
            eq(party),
            eq(expectedDateDue),
            eq(expectedTag),
            eq(Event.REQUEST_HEARING_REQUIREMENTS_FEATURE.toString())
        );

        verify(asylumCase, times(1)).write(DIRECTIONS, allDirections);
    }

    @Test
    void should_add_new_direction_to_the_case_when_no_directions_exist() {

        final List<IdValue<Direction>> allDirections = new ArrayList<>();

        final String expectedExplanationPart =
            "Visit the online service and use the HMCTS reference to find the case. You'll be able to submit the hearing requirements by following the instructions on the overview tab.";
        final Parties expectedParties = Parties.LEGAL_REPRESENTATIVE;
        final String expectedDateDue = "2018-12-25";
        final DirectionTag expectedTag = DirectionTag.LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS;

        when(dateProvider.now()).thenReturn(LocalDate.parse("2018-12-20"));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_HEARING_REQUIREMENTS_FEATURE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.empty());
        when(directionAppender.append(
            eq(asylumCase),
            any(List.class),
            contains(expectedExplanationPart),
            eq(expectedParties),
            eq(expectedDateDue),
            eq(expectedTag),
            eq(Event.REQUEST_HEARING_REQUIREMENTS_FEATURE.toString())
        )).thenReturn(allDirections);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            requestHearingRequirementsDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(directionAppender, times(1)).append(
            eq(asylumCase),
            existingDirectionsCaptor.capture(),
            contains(expectedExplanationPart),
            eq(expectedParties),
            eq(expectedDateDue),
            eq(expectedTag),
            eq(Event.REQUEST_HEARING_REQUIREMENTS_FEATURE.toString())
        );

        List<IdValue<Direction>> actualExistingDirections =
            existingDirectionsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, actualExistingDirections.size());

        verify(asylumCase, times(1)).write(DIRECTIONS, allDirections);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> requestHearingRequirementsDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(
            () -> requestHearingRequirementsDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = requestHearingRequirementsDirectionHandler.canHandle(callbackStage, callback);

                if (event == Event.REQUEST_HEARING_REQUIREMENTS_FEATURE
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

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

        assertThatThrownBy(() -> requestHearingRequirementsDirectionHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> requestHearingRequirementsDirectionHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestHearingRequirementsDirectionHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> requestHearingRequirementsDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    static Stream<Arguments> caseTypeScenarios() {
        return Stream.of(
                Arguments.of(NO, NO, AIP, Parties.APPELLANT),
                Arguments.of(YES, YES, AIP, Parties.APPELLANT),
                Arguments.of(YES, NO, REP, Parties.APPELLANT),
                Arguments.of(YES, YES, REP, Parties.LEGAL_REPRESENTATIVE),
                Arguments.of(NO, NO, REP, Parties.LEGAL_REPRESENTATIVE),
                Arguments.of(NO, YES, REP, Parties.LEGAL_REPRESENTATIVE));
    }
}
