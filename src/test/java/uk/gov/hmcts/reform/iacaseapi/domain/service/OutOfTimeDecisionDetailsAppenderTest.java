package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfTimeDecisionDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfTimeDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
class OutOfTimeDecisionDetailsAppenderTest {

    @Mock private Document document;
    private OutOfTimeDecisionDetailsAppender appender;

    @BeforeEach
    void setUp() {
        appender = new OutOfTimeDecisionDetailsAppender();
    }

    @Test
    void should_return_null_initially() {
        assertNull(appender.getAllOutOfTimeDecisionDetails());
    }

    @Test
    void should_clear_decisions() {
        appender.clearAllOutOfTimeDecisionDetails();
        assertNull(appender.getAllOutOfTimeDecisionDetails());
    }

    @Test
    void should_append_to_empty_list() {
        OutOfTimeDecisionDetails decision = new OutOfTimeDecisionDetails(
            OutOfTimeDecisionType.APPROVED.name(),
            UserRoleLabel.TRIBUNAL_CASEWORKER.name(),
            document
        );
        
        List<IdValue<OutOfTimeDecisionDetails>> result = appender.append(new ArrayList<>(), decision);
        
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
        assertEquals(decision, result.get(0).getValue());
    }

    @Test
    void should_append_to_existing_list() {
        OutOfTimeDecisionDetails existingDecision = new OutOfTimeDecisionDetails(
            OutOfTimeDecisionType.REJECTED.name(),
            UserRoleLabel.LEGAL_REPRESENTATIVE.name(),
            document
        );
        
        OutOfTimeDecisionDetails newDecision = new OutOfTimeDecisionDetails(
            OutOfTimeDecisionType.APPROVED.name(),
            UserRoleLabel.TRIBUNAL_CASEWORKER.name(),
            document
        );
        
        List<IdValue<OutOfTimeDecisionDetails>> existing = Arrays.asList(
            new IdValue<>("1", existingDecision)
        );
        
        List<IdValue<OutOfTimeDecisionDetails>> result = appender.append(existing, newDecision);
        
        assertEquals(2, result.size());
        assertEquals("2", result.get(0).getId());
        assertEquals(newDecision, result.get(0).getValue());
        assertEquals("1", result.get(1).getId());
        assertEquals(existingDecision, result.get(1).getValue());
    }
}
