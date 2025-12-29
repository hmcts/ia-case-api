package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.HearingDecision;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CURRENT_HEARING_ID;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DECISION_GRANTED_OR_REFUSED;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HEARING_DECISION_LIST;

@ExtendWith(MockitoExtension.class)
class HearingDecisionProcessorTest {
    @Mock
    private BailCase bailCase;

    @Captor
    private ArgumentCaptor<List<IdValue<HearingDecision>>> newHearingDecisionListArgumentCaptor;

    private HearingDecisionProcessor hearingDecisionProcessor;

    @BeforeEach
    void setUp() {
        hearingDecisionProcessor = new HearingDecisionProcessor();
    }

    @Test
    void processHearingDecision_should_add_hearing_decision_to_empty_list() {
        // given
        given(bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class)).willReturn(Optional.of("granted"));
        given(bailCase.read(CURRENT_HEARING_ID, String.class)).willReturn(Optional.of("12345"));
        given(bailCase.read(HEARING_DECISION_LIST)).willReturn(Optional.empty());

        // when
        hearingDecisionProcessor.processHearingDecision(bailCase);

        // then
        verify(bailCase).write(eq(HEARING_DECISION_LIST), newHearingDecisionListArgumentCaptor.capture());
        List<IdValue<HearingDecision>> hearingDecisionList = newHearingDecisionListArgumentCaptor.getValue();
        assertEquals(1, hearingDecisionList.size());
        assertEquals("1", hearingDecisionList.get(0).getId());
        assertEquals("12345", hearingDecisionList.get(0).getValue().getHearingId());
        assertEquals("granted", hearingDecisionList.get(0).getValue().getDecision());
    }

    @Test
    void processHearingDecision_should_add_hearing_decision_to_existing_list() {
        // given
        given(bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class)).willReturn(Optional.of("refused"));
        given(bailCase.read(CURRENT_HEARING_ID, String.class)).willReturn(Optional.of("12345"));

        HearingDecision hearingDecision1 = new HearingDecision("23456", "refused");
        HearingDecision hearingDecision2 = new HearingDecision("34567", "refused");
        List<IdValue<HearingDecision>> existingHearingDecisionList = List.of(
                new IdValue<>("1", hearingDecision1),
                new IdValue<>("2", hearingDecision2)
        );
        given(bailCase.read(HEARING_DECISION_LIST)).willReturn(Optional.of(existingHearingDecisionList));

        // when
        hearingDecisionProcessor.processHearingDecision(bailCase);

        // then
        verify(bailCase).write(eq(HEARING_DECISION_LIST), newHearingDecisionListArgumentCaptor.capture());
        List<IdValue<HearingDecision>> hearingDecisionList = newHearingDecisionListArgumentCaptor.getValue();
        assertEquals(3, hearingDecisionList.size());
        assertEquals("1", hearingDecisionList.get(0).getId());
        assertEquals("23456", hearingDecisionList.get(0).getValue().getHearingId());
        assertEquals("refused", hearingDecisionList.get(0).getValue().getDecision());
        assertEquals("2", hearingDecisionList.get(1).getId());
        assertEquals("34567", hearingDecisionList.get(1).getValue().getHearingId());
        assertEquals("refused", hearingDecisionList.get(1).getValue().getDecision());
        assertEquals("3", hearingDecisionList.get(2).getId());
        assertEquals("12345", hearingDecisionList.get(2).getValue().getHearingId());
        assertEquals("refused", hearingDecisionList.get(2).getValue().getDecision());
    }

    @Test
    void processHearingDecision_should_update_hearing_decision_in_existing_list() {
        // given
        given(bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class)).willReturn(Optional.of("conditionalGrant"));
        given(bailCase.read(CURRENT_HEARING_ID, String.class)).willReturn(Optional.of("12345"));

        HearingDecision hearingDecision1 = new HearingDecision("12345", "dismissed");
        HearingDecision hearingDecision2 = new HearingDecision("34567", "dismissed");
        List<IdValue<HearingDecision>> existingHearingDecisionList = List.of(
                new IdValue<>("1", hearingDecision1),
                new IdValue<>("2", hearingDecision2)
        );
        given(bailCase.read(HEARING_DECISION_LIST)).willReturn(Optional.of(existingHearingDecisionList));

        // when
        hearingDecisionProcessor.processHearingDecision(bailCase);

        // then
        verify(bailCase).write(eq(HEARING_DECISION_LIST), newHearingDecisionListArgumentCaptor.capture());
        List<IdValue<HearingDecision>> hearingDecisionList = newHearingDecisionListArgumentCaptor.getValue();
        assertEquals(2, hearingDecisionList.size());
        assertEquals("1", hearingDecisionList.get(0).getId());
        assertEquals("12345", hearingDecisionList.get(0).getValue().getHearingId());
        assertEquals("conditionalGrant", hearingDecisionList.get(0).getValue().getDecision());
        assertEquals("2", hearingDecisionList.get(1).getId());
        assertEquals("34567", hearingDecisionList.get(1).getValue().getHearingId());
        assertEquals("dismissed", hearingDecisionList.get(1).getValue().getDecision());
    }

    @Test
    void processHearingDecision_should_add_decided_if_no_decision() {
        // given
        given(bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class)).willReturn(Optional.empty());
        given(bailCase.read(CURRENT_HEARING_ID, String.class)).willReturn(Optional.of("12345"));
        given(bailCase.read(HEARING_DECISION_LIST)).willReturn(Optional.empty());

        // when
        hearingDecisionProcessor.processHearingDecision(bailCase);

        // then
        verify(bailCase).write(eq(HEARING_DECISION_LIST), newHearingDecisionListArgumentCaptor.capture());
        List<IdValue<HearingDecision>> hearingDecisionList = newHearingDecisionListArgumentCaptor.getValue();
        assertEquals(1, hearingDecisionList.size());
        assertEquals("1", hearingDecisionList.get(0).getId());
        assertEquals("12345", hearingDecisionList.get(0).getValue().getHearingId());
        assertEquals("decided", hearingDecisionList.get(0).getValue().getDecision());
    }

    @Test
    void processHearingDecision_should_not_add_hearing_decision_if_no_current_hearing_id() {
        // given
        given(bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class)).willReturn(Optional.of("granted"));
        given(bailCase.read(CURRENT_HEARING_ID, String.class)).willReturn(Optional.empty());

        // when
        hearingDecisionProcessor.processHearingDecision(bailCase);

        // then
        verify(bailCase).read(DECISION_GRANTED_OR_REFUSED, String.class);
        verify(bailCase).read(CURRENT_HEARING_ID, String.class);
        verifyNoMoreInteractions(bailCase);
    }
}
