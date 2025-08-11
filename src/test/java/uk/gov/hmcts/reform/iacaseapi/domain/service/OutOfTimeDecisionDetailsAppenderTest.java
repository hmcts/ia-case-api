package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
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
        // Tests line 25: return allOutOfTimeDecisionDetails;
        assertNull(appender.getAllOutOfTimeDecisionDetails());
    }

    @Test
    void should_append_decision() {
        // Tests line 40: allOutOfTimeDecisionDetails = new ArrayList<>();
        OutOfTimeDecisionDetails decision = new OutOfTimeDecisionDetails(
            OutOfTimeDecisionType.APPROVED.name(),
            UserRoleLabel.TRIBUNAL_CASEWORKER.name(),
            document
        );
        
        List<IdValue<OutOfTimeDecisionDetails>> result = appender.append(new ArrayList<>(), decision);
        
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }
}
