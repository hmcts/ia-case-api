package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ACTUAL_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_APPELLANT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_JUDGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_INTEGRATED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LISTING_LENGTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_DATE_DUE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_EXPLANATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_PARTIES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HoursAndMinutes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HoursMinutes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PreviousHearingAppender;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RequestNewHearingRequirementsDirectionHandlerTest {

    private static final int HEARING_REQUIREMENTS_DUE_IN_DAYS = 5;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private DirectionAppender directionAppender;
    @Mock
    private PreviousHearingAppender previousHearingAppender;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private DocumentWithMetadata hearingRequirements1;
    private RequestNewHearingRequirementsDirectionHandler requestNewHearingRequirementsDirectionHandler;

    @BeforeEach
    public void setUp() {
        requestNewHearingRequirementsDirectionHandler =
            new RequestNewHearingRequirementsDirectionHandler(
                HEARING_REQUIREMENTS_DUE_IN_DAYS,
                dateProvider,
                directionAppender,
                previousHearingAppender,
                featureToggler
            );
    }

    @Test
    void can_handle_request_new_hearing_requirements() {

        when(callback.getEvent()).thenReturn(Event.REQUEST_NEW_HEARING_REQUIREMENTS);
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);

        requestNewHearingRequirementsDirectionHandler =
            new RequestNewHearingRequirementsDirectionHandler(
                HEARING_REQUIREMENTS_DUE_IN_DAYS,
                dateProvider,
                directionAppender,
                previousHearingAppender,
                featureToggler
            );

        boolean canHandle =
            requestNewHearingRequirementsDirectionHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertTrue(canHandle);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_append_new_direction_to_existing_directions_for_the_case(YesOrNo isIntegrated) {

        final List<IdValue<Direction>> existingDirections = new ArrayList<>();
        final List<IdValue<Direction>> allDirections = new ArrayList<>();
        final List<IdValue<DocumentWithMetadata>> hearingRequirements =
            singletonList(new IdValue<>("1", hearingRequirements1));

        final String expectedExplanation = "Do the thing";
        final Parties expectedParties = Parties.LEGAL_REPRESENTATIVE;
        final String expectedDateDue = "2020-10-06";
        final DirectionTag expectedDirectionTag = DirectionTag.REQUEST_NEW_HEARING_REQUIREMENTS;
        final Event event = Event.REQUEST_NEW_HEARING_REQUIREMENTS;
        final String attendingJudge = "Judge Johnson";
        final String attendingAppellant = "Joe Bloggs";
        final String attendingHomeOfficeLegalRepresentative = "Jim Smith";
        final HoursAndMinutes actualCaseHearingLength = new HoursAndMinutes("5", "30");
        final String ariaListingReference = "LP/12345/2020";
        final HearingCentre listCaseHearingCentre = HearingCentre.NEWPORT;
        final String listCaseHearingDate = "13/10/2020";
        final String listCaseHearingLength = "6 hours";
        final String appealDecision = "Dismissed";

        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-10-01"));

        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(existingDirections));
        when(asylumCase.read(SEND_DIRECTION_EXPLANATION, String.class)).thenReturn(Optional.of(expectedExplanation));
        when(asylumCase.read(SEND_DIRECTION_DATE_DUE, String.class)).thenReturn(Optional.of(expectedDateDue));
        when(asylumCase.read(ATTENDING_JUDGE, String.class)).thenReturn(Optional.of(attendingJudge));
        when(asylumCase.read(ATTENDING_APPELLANT, String.class)).thenReturn(Optional.of(attendingAppellant));
        when(asylumCase.read(ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE, String.class))
            .thenReturn(Optional.of(attendingHomeOfficeLegalRepresentative));
        when(asylumCase.read(ACTUAL_CASE_HEARING_LENGTH, HoursAndMinutes.class))
            .thenReturn(Optional.of(actualCaseHearingLength));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(listCaseHearingCentre));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listCaseHearingDate));
        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(isIntegrated));

        if (isIntegrated.equals(YES)) {
            when(asylumCase.read(LISTING_LENGTH, HoursMinutes.class)).thenReturn(Optional.of(new HoursMinutes(6,0)));
        } else {
            when(asylumCase.read(LIST_CASE_HEARING_LENGTH, String.class)).thenReturn(Optional.of(listCaseHearingLength));
        }

        when(asylumCase.read(APPEAL_DECISION, String.class)).thenReturn(Optional.of(appealDecision));

        when(directionAppender.append(
            asylumCase,
            existingDirections,
            expectedExplanation,
            expectedParties,
            expectedDateDue,
            expectedDirectionTag
        )).thenReturn(allDirections);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            requestNewHearingRequirementsDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(directionAppender, times(1)).append(
            eq(asylumCase),
            eq(existingDirections),
            contains(expectedExplanation),
            eq(expectedParties),
            eq(expectedDateDue),
            eq(expectedDirectionTag)
        );

        verify(asylumCase, times(1)).read(SEND_DIRECTION_EXPLANATION, String.class);
        verify(asylumCase, times(1)).read(SEND_DIRECTION_DATE_DUE, String.class);
        verify(asylumCase, times(1)).write(DIRECTIONS, allDirections);

        verify(asylumCase).clear(SEND_DIRECTION_EXPLANATION);
        verify(asylumCase).clear(SEND_DIRECTION_PARTIES);
        verify(asylumCase).clear(SEND_DIRECTION_DATE_DUE);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> requestNewHearingRequirementsDirectionHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> requestNewHearingRequirementsDirectionHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = requestNewHearingRequirementsDirectionHandler.canHandle(callbackStage, callback);

                if (event == Event.REQUEST_NEW_HEARING_REQUIREMENTS
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

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

                boolean canHandle = requestNewHearingRequirementsDirectionHandler.canHandle(callbackStage, callback);

                if (event == Event.REQUEST_NEW_HEARING_REQUIREMENTS
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> requestNewHearingRequirementsDirectionHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> requestNewHearingRequirementsDirectionHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestNewHearingRequirementsDirectionHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> requestNewHearingRequirementsDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}