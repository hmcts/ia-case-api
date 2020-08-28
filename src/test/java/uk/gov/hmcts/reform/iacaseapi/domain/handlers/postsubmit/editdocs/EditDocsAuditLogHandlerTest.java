package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_CLARIFYING_QUESTION_ANSWERS;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@ExtendWith(MockitoExtension.class)
class EditDocsAuditLogHandlerTest {

    @InjectMocks
    EditDocsAuditLogHandler editDocsAuditLogHandler;
    @Mock private
    EditDocsAuditLogService editDocsAuditLogService;
    @Mock private
    Callback<AsylumCase> callback;
    @Mock private
    CaseDetails<AsylumCase> caseDetails;
    @Mock private
    CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock private
    AsylumCase asylumCase;
    @Mock private
    AsylumCase asylumCaseBefore;

    @ParameterizedTest
    @MethodSource("canHandleTestData")
    void canHandle(Event event, boolean expectedResult) {
        when(callback.getEvent()).thenReturn(event);

        boolean actualResult = editDocsAuditLogHandler.canHandle(callback);

        assertEquals(expectedResult, actualResult);
    }

    private static Stream<Arguments> canHandleTestData() {

        List<Arguments> scenarios = new ArrayList<>();

        scenarios.add(Arguments.of(EDIT_DOCUMENTS, true));
        scenarios.add(Arguments.of(SUBMIT_CLARIFYING_QUESTION_ANSWERS, false));

        return scenarios.stream();
    }

    @Test
    void given_null_callback_should_throw_exception() {
        Assertions.assertThatThrownBy(() -> editDocsAuditLogHandler.canHandle(null))
            .isExactlyInstanceOf(NullPointerException.class);
    }


    @Test
    void handle() {
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

    void mockServiceDependency() {
        BDDMockito.given(editDocsAuditLogService.buildAuditDetails(eq(1234L), any(AsylumCase.class), any()))
            .willReturn(AuditDetails.builder().build());
    }

    void mockCallbackAndCaseDetails() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(1234L);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);
    }
}
