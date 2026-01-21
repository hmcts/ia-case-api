package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@ExtendWith(MockitoExtension.class)
class EditBailDocsAuditLogHandlerTest {

    @InjectMocks
    private EditBailDocsAuditLogHandler editBailDocsAuditLogHandler;
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


    @ParameterizedTest
    @CsvSource({
        "EDIT_DOCUMENTS, true",
        "SUBMIT_CLARIFYING_QUESTION_ANSWERS, false"
    })
    void canHandle(Event event, boolean expectedResult) {
        when(callback.getEvent()).thenReturn(event);

        boolean actualResult = editBailDocsAuditLogHandler.canHandle(callback);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void given_null_callback_should_throw_exception() {
        assertThrows(NullPointerException.class, () -> {
            editBailDocsAuditLogHandler.canHandle(null);
        });
    }


    @Test
    void handle() {
        Logger fooLogger = (Logger) LoggerFactory.getLogger(EditBailDocsAuditLogHandler.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        fooLogger.addAppender(listAppender);

        mockCallbackAndCaseDetails();
        mockServiceDependency();

        editBailDocsAuditLogHandler.handle(callback);

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals("INFO", logsList.get(0).getLevel().toString());
        assertEquals("Edit Document audit logs: AuditDetails(idamUserId=null, user=null, documentIds=null, "
            + "documentNames=null, caseId=0, reason=null, dateTime=null)", logsList.get(0).getFormattedMessage());
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
