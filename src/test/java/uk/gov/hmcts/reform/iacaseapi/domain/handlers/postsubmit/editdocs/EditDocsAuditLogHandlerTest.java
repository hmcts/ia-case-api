package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@RunWith(JUnitParamsRunner.class)
public class EditDocsAuditLogHandlerTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @InjectMocks
    private EditDocsAuditLogHandler editDocsAuditLogHandler;
    @Mock
    private EditDocsAuditLogService editDocsAuditLogService;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AsylumCase asylumCaseBefore;

    @Test
    @Parameters({
        "EDIT_DOCUMENTS, true",
        "SUBMIT_CLARIFYING_ANSWERS, false"
    })
    public void canHandle(Event event, boolean expectedResult) {
        when(callback.getEvent()).thenReturn(event);

        boolean actualResult = editDocsAuditLogHandler.canHandle(callback);

        assertEquals(expectedResult, actualResult);
    }

    @Test(expected = NullPointerException.class)
    public void given_null_callback_should_throw_exception() {
        editDocsAuditLogHandler.canHandle(null);
    }


    @Test
    public void handle() {
        Logger fooLogger = (Logger) LoggerFactory.getLogger(EditDocsAuditLogHandler.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        fooLogger.addAppender(listAppender);

        mockCallbackAndCaseDetails();
        mockServiceDependency();

        editDocsAuditLogHandler.handle(callback);

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals("INFO", logsList.get(0).getLevel().toString());
        assertEquals("Edit Document audit logs: AuditDetails(idamUserId=null, user=null, documentIds=null, "
            + "caseId=0, reason=null, dateTime=null)", logsList.get(0).getFormattedMessage());
    }

    private void mockServiceDependency() {
        BDDMockito.given(editDocsAuditLogService.buildAuditDetails(eq(1234L), any(AsylumCase.class), any()))
            .willReturn(AuditDetails.builder().build());
    }

    private void mockCallbackAndCaseDetails() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(1234L);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);
    }
}