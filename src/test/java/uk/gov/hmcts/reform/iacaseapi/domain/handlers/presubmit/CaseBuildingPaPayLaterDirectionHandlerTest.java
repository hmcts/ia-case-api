package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HoursAndMinutes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HoursMinutes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PreviousHearingAppender;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre.DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre.NEWPORT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CaseBuildingPaPayLaterDirectionHandlerTest {

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
    private CaseBuildingPaPayLaterDirectionHandler caseBuildingPaPayLaterDirectionHandler;

    @BeforeEach
    public void setUp() {
        caseBuildingPaPayLaterDirectionHandler =
                new CaseBuildingPaPayLaterDirectionHandler(
                        HEARING_REQUIREMENTS_DUE_IN_DAYS,
                        dateProvider,
                        directionAppender
                );
    }

    @Test
    void should_handle_case_building_aip_pay_later() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.CASE_BUILDING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class))
                .thenReturn(Optional.of(JourneyType.AIP));

        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class))
                .thenReturn(Optional.of("payLater"));

        boolean canHandle = decidedPaPayLaterDirectionHandler.canHandle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
        );

        assertTrue(canHandle);
    }

    @Test
    void should_handle_case_building_lr_pay_later() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.CASE_BUILDING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class))
                .thenReturn(Optional.of(JourneyType.REP));

        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class))
                .thenReturn(Optional.of("payLater"));

        boolean canHandle = decidedPaPayLaterDirectionHandler.canHandle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
        );

        assertTrue(canHandle);
    }

    @Test
    void should_append_pay_later_direction() {
        List<IdValue<Direction>> existingDirections = new ArrayList<>();
        List<IdValue<Direction>> allDirections = new ArrayList<>();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getState()).thenReturn(State.CASE_BUILDING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.parse("2024-04-01"));

        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class))
                .thenReturn(Optional.of("payLater"));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class))
                .thenReturn(Optional.of(JourneyType.REP));

        when(asylumCase.read(DIRECTIONS))
                .thenReturn(Optional.of(existingDirections));

        when(directionAppender.append(
                eq(asylumCase),
                eq(existingDirections),
                contains("Your appeal requires a fee to be paid"),
                eq(Parties.LEGAL_REPRESENTATIVE),
                eq("2024-04-06"),
                eq(DirectionTag.LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS),
                eq(Event.SUBMIT_APPEAL.toString())
        )).thenReturn(allDirections);

        PreSubmitCallbackResponse<AsylumCase> response =
                caseBuildingPaPayLaterDirectionHandler.handle(
                        PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                        callback
                );

        assertEquals(asylumCase, response.getData());
        verify(asylumCase).write(DIRECTIONS, allDirections);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> caseBuildingPaPayLaterDirectionHandler
                .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> caseBuildingPaPayLaterDirectionHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
                () -> caseBuildingPaPayLaterDirectionHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseBuildingPaPayLaterDirectionHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
                () -> caseBuildingPaPayLaterDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }
}