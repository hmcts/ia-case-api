package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.beust.jcommander.internal.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Application;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ClarifyingQuestion;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.EditableDirection;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PreviousDates;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.WaFieldsPublisher;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ChangeBailDirectionDueDateHandlerTest {

    @Mock
    private DateProvider dateProvider;
    @Mock
    private WaFieldsPublisher waFieldsPublisher;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Captor
    private ArgumentCaptor<List<IdValue<Direction>>> asylumValueCaptor;
    @Captor
    private ArgumentCaptor<AsylumCaseFieldDefinition> asylumExtractorCaptor;
    @Captor
    private ArgumentCaptor<List<IdValue<Application>>> applicationsCaptor;
    @Captor
    private ArgumentCaptor<List<IdValue<Parties>>> directionEditPartiesCaptor;

    private String applicationSupplier = "Legal representative";
    private String applicationReason = "applicationReason";
    private String applicationDate = "09/01/2020";
    private String applicationDecision = "Granted";
    private String applicationDecisionReason = "Granted";
    private String applicationDateOfDecision = "10/01/2020";
    private String applicationStatus = "In progress";

    private List<IdValue<Application>> applications = Lists.newArrayList(new IdValue<>("1", new Application(
        Collections.emptyList(),
        applicationSupplier,
        ApplicationType.TIME_EXTENSION.toString(),
        applicationReason,
        applicationDate,
        applicationDecision,
        applicationDecisionReason,
        applicationDateOfDecision,
        applicationStatus
    )));

    private String direction1 = "Direction 1";
    private LocalDate dateSent = LocalDate.now();

    private ChangeBailDirectionDueDateHandler changeBailDirectionDueDateHandler;

    @BeforeEach
    public void setUp() {
        when(dateProvider.now()).thenReturn(dateSent);

        changeBailDirectionDueDateHandler =
            new ChangeBailDirectionDueDateHandler(dateProvider,waFieldsPublisher);
    }

    @Test
    void should_copy_due_date_back_into_main_direction_fields_ignoring_other_changes() {

        List<IdValue<Direction>> existingDirections =
               Arrays.asList(
                new IdValue<>("1", new Direction(
                    "explanation-1",
                    Parties.LEGAL_REPRESENTATIVE,
                    "2020-12-01",
                    "2019-12-01",
                    DirectionTag.LEGAL_REPRESENTATIVE_REVIEW,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    UUID.randomUUID().toString(),
                    "directionType1"
                )),


                new IdValue<>("2", new Direction(
                    "explanation-2",
                    Parties.RESPONDENT,
                    "2020-11-01",
                    "2019-11-01",
                    DirectionTag.RESPONDENT_REVIEW,
                    newArrayList(new IdValue<>("1", new PreviousDates("2018-05-01", "2018-03-01"))),
                    newArrayList(new IdValue<>("1", new ClarifyingQuestion("is this a sample question?"))),
                    UUID.randomUUID().toString(),
                    "directionType2"
                ))
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CHANGE_DIRECTION_DUE_DATE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(existingDirections));

        // "Direction 1" in UI is equivalent of Direction with IdValue "2" in backend
        when(asylumCase.read(DIRECTION_LIST, DynamicList.class)).thenReturn(Optional.of(new DynamicList(direction1)));
        when(asylumCase.read(DIRECTION_EDIT_DATE_DUE, String.class)).thenReturn(Optional.of("2222-12-01"));
        when(asylumCase.read(APPLICATIONS)).thenReturn(Optional.of(applications));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            changeBailDirectionDueDateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(3)).write(asylumExtractorCaptor.capture(), asylumValueCaptor.capture());

        verify(asylumCase).clear(DIRECTION_LIST);
        verify(asylumCase).clear(DISABLE_OVERVIEW_PAGE);
        verify(asylumCase).clear(APPLICATION_TIME_EXTENSION_EXISTS);
        verify(asylumCase).write(eq(APPLICATIONS), applicationsCaptor.capture());
        verify(asylumCase).write(eq(DIRECTION_EDIT_PARTIES), directionEditPartiesCaptor.capture());
        assertEquals("Completed", applicationsCaptor.getValue().get(0).getValue().getApplicationStatus());
        verify(waFieldsPublisher).addLastModifiedDirection(
                eq(asylumCase), anyString(), any(Parties.class), anyString(), any(DirectionTag.class), anyString(), anyString());

        List<List<IdValue<Direction>>> asylumCaseValues = asylumValueCaptor.getAllValues();
        List<AsylumCaseFieldDefinition> asylumCaseFieldDefinitions = asylumExtractorCaptor.getAllValues();
        List<IdValue<Direction>> actualDirections = asylumCaseValues.get(asylumCaseFieldDefinitions.indexOf(DIRECTIONS));
        assertEquals(existingDirections.size(), actualDirections.size());

        assertEquals("1", actualDirections.get(0).getId());
        assertEquals("explanation-1", actualDirections.get(0).getValue().getExplanation());
        assertEquals(Parties.LEGAL_REPRESENTATIVE, actualDirections.get(0).getValue().getParties());
        assertEquals("2020-12-01", actualDirections.get(0).getValue().getDateDue());
        assertEquals("2019-12-01", actualDirections.get(0).getValue().getDateSent());
        assertEquals(Collections.emptyList(), actualDirections.get(0).getValue().getClarifyingQuestions());
        assertEquals(DirectionTag.LEGAL_REPRESENTATIVE_REVIEW, actualDirections.get(0).getValue().getTag());

        // "Direction 1" in UI is equivalent of Direction with IdValue "2" in backend
        assertEquals("2", actualDirections.get(1).getId());
        assertEquals("explanation-2", actualDirections.get(1).getValue().getExplanation());
        assertEquals(Parties.RESPONDENT, actualDirections.get(1).getValue().getParties());
        assertEquals("2222-12-01", actualDirections.get(1).getValue().getDateDue());
        assertEquals(dateSent.toString(), actualDirections.get(1).getValue().getDateSent());
        assertEquals(DirectionTag.RESPONDENT_REVIEW, actualDirections.get(1).getValue().getTag());
        assertEquals(2, actualDirections.get(1).getValue().getPreviousDates().size());
        assertEquals("2", actualDirections.get(1).getValue().getPreviousDates().get(0).getId());
        assertEquals("2020-11-01",
            actualDirections.get(1).getValue().getPreviousDates().get(0).getValue().getDateDue());
        assertEquals("2019-11-01",
            actualDirections.get(1).getValue().getPreviousDates().get(0).getValue().getDateSent());
        assertEquals("1", actualDirections.get(1).getValue().getPreviousDates().get(1).getId());
        assertEquals("2018-05-01",
            actualDirections.get(1).getValue().getPreviousDates().get(1).getValue().getDateDue());
        assertEquals("2018-03-01",
            actualDirections.get(1).getValue().getPreviousDates().get(1).getValue().getDateSent());
        assertEquals(1, actualDirections.get(1).getValue().getClarifyingQuestions().size());
        assertEquals("1", actualDirections.get(1).getValue().getClarifyingQuestions().get(0).getId());
        assertEquals("is this a sample question?",
                actualDirections.get(1).getValue().getClarifyingQuestions().get(0).getValue().getQuestion());

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> changeBailDirectionDueDateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> changeBailDirectionDueDateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = changeBailDirectionDueDateHandler.canHandle(callbackStage, callback);

                if (event == Event.CHANGE_DIRECTION_DUE_DATE
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

        assertThatThrownBy(() -> changeBailDirectionDueDateHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeBailDirectionDueDateHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeBailDirectionDueDateHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeBailDirectionDueDateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    // remove when new CCD definitions are in Prod
    @Test
    void should_copy_due_date_back_into_main_direction_fields_ignoring_other_changes_deprecated_path() {

        final List<IdValue<Direction>> existingDirections =
            Arrays.asList(
                new IdValue<>("1", new Direction(
                    "explanation-1",
                    Parties.LEGAL_REPRESENTATIVE,
                    "2020-12-01",
                    "2019-12-01",
                    DirectionTag.LEGAL_REPRESENTATIVE_REVIEW,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    UUID.randomUUID().toString(),
                    "directionType1"
                )),
                new IdValue<>("2", new Direction(
                    "explanation-2",
                    Parties.RESPONDENT,
                    "2020-11-01",
                    "2019-11-01",
                    DirectionTag.RESPONDENT_REVIEW,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    UUID.randomUUID().toString(),
                    "directionType2"
                ))
            );

        final List<IdValue<EditableDirection>> editableDirections =
            Arrays.asList(
                new IdValue<>("1", new EditableDirection(
                    "some-other-explanation-1-that-should-be-ignored",
                    Parties.BOTH,
                    "2222-12-01"
                )),
                new IdValue<>("2", new EditableDirection(
                    "some-other-explanation-1-that-should-be-ignored",
                    Parties.BOTH,
                    "3333-11-01"
                ))
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CHANGE_DIRECTION_DUE_DATE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(existingDirections));
        when(asylumCase.read(EDITABLE_DIRECTIONS)).thenReturn(Optional.of(editableDirections));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            changeBailDirectionDueDateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(
            asylumExtractorCaptor.capture(),
            asylumValueCaptor.capture());

        List<AsylumCaseFieldDefinition> asylumCaseFieldDefinitions = asylumExtractorCaptor.getAllValues();
        List<List<IdValue<Direction>>> asylumCaseValues = asylumValueCaptor.getAllValues();

        List<IdValue<Direction>> actualDirections =
            asylumCaseValues.get(asylumCaseFieldDefinitions.indexOf(DIRECTIONS));

        assertEquals(
            existingDirections.size(),
            actualDirections.size()
        );

        assertEquals("1", actualDirections.get(0).getId());
        assertEquals("explanation-1", actualDirections.get(0).getValue().getExplanation());
        assertEquals(Parties.LEGAL_REPRESENTATIVE, actualDirections.get(0).getValue().getParties());
        assertEquals("2222-12-01", actualDirections.get(0).getValue().getDateDue());
        assertEquals("2019-12-01", actualDirections.get(0).getValue().getDateSent());
        assertEquals(DirectionTag.LEGAL_REPRESENTATIVE_REVIEW, actualDirections.get(0).getValue().getTag());

        assertEquals("2", actualDirections.get(1).getId());
        assertEquals("explanation-2", actualDirections.get(1).getValue().getExplanation());
        assertEquals(Parties.RESPONDENT, actualDirections.get(1).getValue().getParties());
        assertEquals("3333-11-01", actualDirections.get(1).getValue().getDateDue());
        assertEquals("2019-11-01", actualDirections.get(1).getValue().getDateSent());
        assertEquals(DirectionTag.RESPONDENT_REVIEW, actualDirections.get(1).getValue().getTag());
    }
}
