package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealDecision.ALLOWED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealDecision.DISMISSED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_HEARING_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_DECISION_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_DECISION_ALLOWED;

@ExtendWith(MockitoExtension.class)
class HearingDecisionProcessorTest {
    @Mock
    private AsylumCase asylumCase;

    @Captor
    private ArgumentCaptor<List<IdValue<HearingDecision>>> newHearingDecisionListArgumentCaptor;

    private HearingDecisionProcessor hearingDecisionProcessor;

    @BeforeEach
    void setUp() {
        hearingDecisionProcessor = new HearingDecisionProcessor();
    }

    @Test
    void processHearingAppealDecision_should_add_hearing_decision_to_empty_list() {
        // given
        given(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)).willReturn(Optional.of(ALLOWED));
        given(asylumCase.read(CURRENT_HEARING_ID, String.class)).willReturn(Optional.of("12345"));
        given(asylumCase.read(HEARING_DECISION_LIST)).willReturn(Optional.empty());

        // when
        hearingDecisionProcessor.processHearingAppealDecision(asylumCase);

        // then
        verify(asylumCase).write(eq(HEARING_DECISION_LIST), newHearingDecisionListArgumentCaptor.capture());
        List<IdValue<HearingDecision>> hearingDecisionList = newHearingDecisionListArgumentCaptor.getValue();
        assertEquals(1, hearingDecisionList.size());
        assertEquals("1", hearingDecisionList.get(0).getId());
        assertEquals("12345", hearingDecisionList.get(0).getValue().getHearingId());
        assertEquals("allowed", hearingDecisionList.get(0).getValue().getDecision());
    }

    @Test
    void processHearingAppealDecision_should_add_hearing_decision_to_existing_list() {
        // given
        given(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)).willReturn(Optional.of(DISMISSED));
        given(asylumCase.read(CURRENT_HEARING_ID, String.class)).willReturn(Optional.of("12345"));

        HearingDecision hearingDecision1 = new HearingDecision("23456", "dismissed");
        HearingDecision hearingDecision2 = new HearingDecision("34567", "dismissed");
        List<IdValue<HearingDecision>> existingHearingDecisionList = List.of(
            new IdValue<>("1", hearingDecision1),
            new IdValue<>("2", hearingDecision2)
        );
        given(asylumCase.read(HEARING_DECISION_LIST)).willReturn(Optional.of(existingHearingDecisionList));

        // when
        hearingDecisionProcessor.processHearingAppealDecision(asylumCase);

        // then
        verify(asylumCase).write(eq(HEARING_DECISION_LIST), newHearingDecisionListArgumentCaptor.capture());
        List<IdValue<HearingDecision>> hearingDecisionList = newHearingDecisionListArgumentCaptor.getValue();
        assertEquals(3, hearingDecisionList.size());
        assertEquals("1", hearingDecisionList.get(0).getId());
        assertEquals("23456", hearingDecisionList.get(0).getValue().getHearingId());
        assertEquals("dismissed", hearingDecisionList.get(0).getValue().getDecision());
        assertEquals("2", hearingDecisionList.get(1).getId());
        assertEquals("34567", hearingDecisionList.get(1).getValue().getHearingId());
        assertEquals("dismissed", hearingDecisionList.get(1).getValue().getDecision());
        assertEquals("3", hearingDecisionList.get(2).getId());
        assertEquals("12345", hearingDecisionList.get(2).getValue().getHearingId());
        assertEquals("dismissed", hearingDecisionList.get(2).getValue().getDecision());
    }

    @Test
    void processHearingAppealDecision_should_update_hearing_decision_in_existing_list() {
        // given
        given(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)).willReturn(Optional.of(ALLOWED));
        given(asylumCase.read(CURRENT_HEARING_ID, String.class)).willReturn(Optional.of("12345"));

        HearingDecision hearingDecision1 = new HearingDecision("12345", "dismissed");
        HearingDecision hearingDecision2 = new HearingDecision("34567", "dismissed");
        List<IdValue<HearingDecision>> existingHearingDecisionList = List.of(
            new IdValue<>("1", hearingDecision1),
            new IdValue<>("2", hearingDecision2)
        );
        given(asylumCase.read(HEARING_DECISION_LIST)).willReturn(Optional.of(existingHearingDecisionList));

        // when
        hearingDecisionProcessor.processHearingAppealDecision(asylumCase);

        // then
        verify(asylumCase).write(eq(HEARING_DECISION_LIST), newHearingDecisionListArgumentCaptor.capture());
        List<IdValue<HearingDecision>> hearingDecisionList = newHearingDecisionListArgumentCaptor.getValue();
        assertEquals(2, hearingDecisionList.size());
        assertEquals("1", hearingDecisionList.get(0).getId());
        assertEquals("12345", hearingDecisionList.get(0).getValue().getHearingId());
        assertEquals("allowed", hearingDecisionList.get(0).getValue().getDecision());
        assertEquals("2", hearingDecisionList.get(1).getId());
        assertEquals("34567", hearingDecisionList.get(1).getValue().getHearingId());
        assertEquals("dismissed", hearingDecisionList.get(1).getValue().getDecision());
    }

    @Test
    void processHearingAppealDecision_should_add_decided_if_no_decision() {
        // given
        given(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)).willReturn(Optional.empty());
        given(asylumCase.read(CURRENT_HEARING_ID, String.class)).willReturn(Optional.of("12345"));
        given(asylumCase.read(HEARING_DECISION_LIST)).willReturn(Optional.empty());

        // when
        hearingDecisionProcessor.processHearingAppealDecision(asylumCase);

        // then
        verify(asylumCase).write(eq(HEARING_DECISION_LIST), newHearingDecisionListArgumentCaptor.capture());
        List<IdValue<HearingDecision>> hearingDecisionList = newHearingDecisionListArgumentCaptor.getValue();
        assertEquals(1, hearingDecisionList.size());
        assertEquals("1", hearingDecisionList.get(0).getId());
        assertEquals("12345", hearingDecisionList.get(0).getValue().getHearingId());
        assertEquals("decided", hearingDecisionList.get(0).getValue().getDecision());
    }

    @Test
    void processHearingAppealDecision_should_not_add_hearing_decision_if_no_current_hearing_id() {
        // given
        given(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)).willReturn(Optional.of(ALLOWED));
        given(asylumCase.read(CURRENT_HEARING_ID, String.class)).willReturn(Optional.empty());

        // when
        hearingDecisionProcessor.processHearingAppealDecision(asylumCase);

        // then
        verify(asylumCase).read(IS_DECISION_ALLOWED, AppealDecision.class);
        verify(asylumCase).read(CURRENT_HEARING_ID, String.class);
        verifyNoMoreInteractions(asylumCase);
    }

    @Test
    void processHearingFtpaDecision_should_add_hearing_decision_to_empty_list() {
        // given
        given(asylumCase.read(CURRENT_HEARING_ID, String.class)).willReturn(Optional.of("12345"));
        given(asylumCase.read(HEARING_DECISION_LIST)).willReturn(Optional.empty());

        // when
        hearingDecisionProcessor.processHearingFtpaDecision(asylumCase, "decision");

        // then
        verify(asylumCase).write(eq(HEARING_DECISION_LIST), newHearingDecisionListArgumentCaptor.capture());
        List<IdValue<HearingDecision>> hearingDecisionList = newHearingDecisionListArgumentCaptor.getValue();
        assertEquals(1, hearingDecisionList.size());
        assertEquals("1", hearingDecisionList.get(0).getId());
        assertEquals("12345", hearingDecisionList.get(0).getValue().getHearingId());
        assertEquals("decision", hearingDecisionList.get(0).getValue().getDecision());
    }

    @Test
    void processHearingFtpaDecision_should_add_hearing_decision_to_existing_list() {
        // given
        given(asylumCase.read(CURRENT_HEARING_ID, String.class)).willReturn(Optional.of("12345"));

        HearingDecision hearingDecision1 = new HearingDecision("23456", "dismissed");
        HearingDecision hearingDecision2 = new HearingDecision("34567", "dismissed");
        List<IdValue<HearingDecision>> existingHearingDecisionList = List.of(
            new IdValue<>("1", hearingDecision1),
            new IdValue<>("2", hearingDecision2)
        );
        given(asylumCase.read(HEARING_DECISION_LIST)).willReturn(Optional.of(existingHearingDecisionList));

        // when
        hearingDecisionProcessor.processHearingFtpaDecision(asylumCase, "decision");

        // then
        verify(asylumCase).write(eq(HEARING_DECISION_LIST), newHearingDecisionListArgumentCaptor.capture());
        List<IdValue<HearingDecision>> hearingDecisionList = newHearingDecisionListArgumentCaptor.getValue();
        assertEquals(3, hearingDecisionList.size());
        assertEquals("1", hearingDecisionList.get(0).getId());
        assertEquals("23456", hearingDecisionList.get(0).getValue().getHearingId());
        assertEquals("dismissed", hearingDecisionList.get(0).getValue().getDecision());
        assertEquals("2", hearingDecisionList.get(1).getId());
        assertEquals("34567", hearingDecisionList.get(1).getValue().getHearingId());
        assertEquals("dismissed", hearingDecisionList.get(1).getValue().getDecision());
        assertEquals("3", hearingDecisionList.get(2).getId());
        assertEquals("12345", hearingDecisionList.get(2).getValue().getHearingId());
        assertEquals("decision", hearingDecisionList.get(2).getValue().getDecision());
    }

    @Test
    void processHearingFtpaDecision_should_update_hearing_decision_in_existing_list() {
        // given
        given(asylumCase.read(CURRENT_HEARING_ID, String.class)).willReturn(Optional.of("12345"));

        HearingDecision hearingDecision1 = new HearingDecision("12345", "dismissed");
        HearingDecision hearingDecision2 = new HearingDecision("34567", "dismissed");
        List<IdValue<HearingDecision>> existingHearingDecisionList = List.of(
            new IdValue<>("1", hearingDecision1),
            new IdValue<>("2", hearingDecision2)
        );
        given(asylumCase.read(HEARING_DECISION_LIST)).willReturn(Optional.of(existingHearingDecisionList));

        // when
        hearingDecisionProcessor.processHearingFtpaDecision(asylumCase, "decision");

        // then
        verify(asylumCase).write(eq(HEARING_DECISION_LIST), newHearingDecisionListArgumentCaptor.capture());
        List<IdValue<HearingDecision>> hearingDecisionList = newHearingDecisionListArgumentCaptor.getValue();
        assertEquals(2, hearingDecisionList.size());
        assertEquals("1", hearingDecisionList.get(0).getId());
        assertEquals("12345", hearingDecisionList.get(0).getValue().getHearingId());
        assertEquals("decision", hearingDecisionList.get(0).getValue().getDecision());
        assertEquals("2", hearingDecisionList.get(1).getId());
        assertEquals("34567", hearingDecisionList.get(1).getValue().getHearingId());
        assertEquals("dismissed", hearingDecisionList.get(1).getValue().getDecision());
    }

    @Test
    void processHearingFtpaDecision_should_add_decided_if_no_decision() {
        // given
        given(asylumCase.read(CURRENT_HEARING_ID, String.class)).willReturn(Optional.of("12345"));
        given(asylumCase.read(HEARING_DECISION_LIST)).willReturn(Optional.empty());

        // when
        hearingDecisionProcessor.processHearingFtpaDecision(asylumCase, "decision");

        // then
        verify(asylumCase).write(eq(HEARING_DECISION_LIST), newHearingDecisionListArgumentCaptor.capture());
        List<IdValue<HearingDecision>> hearingDecisionList = newHearingDecisionListArgumentCaptor.getValue();
        assertEquals(1, hearingDecisionList.size());
        assertEquals("1", hearingDecisionList.get(0).getId());
        assertEquals("12345", hearingDecisionList.get(0).getValue().getHearingId());
        assertEquals("decision", hearingDecisionList.get(0).getValue().getDecision());
    }

    @Test
    void processHearingFtpaDecision_should_not_add_hearing_decision_if_no_current_hearing_id() {
        // given
        given(asylumCase.read(CURRENT_HEARING_ID, String.class)).willReturn(Optional.empty());

        // when
        hearingDecisionProcessor.processHearingFtpaDecision(asylumCase, "decision");

        // then
        verify(asylumCase).read(CURRENT_HEARING_ID, String.class);
        verifyNoMoreInteractions(asylumCase);
    }
}