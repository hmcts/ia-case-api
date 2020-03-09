package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.groups.Tuple;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
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
    private EditDocsAuditService editDocsAuditService = new EditDocsAuditService();
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
    @Mock
    private UserDetails userDetails;
    @Mock
    private UserDetailsProvider userDetailsProvider;

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
        mockUserDetailsProvider();
        mockAuditServiceDependencies();

        editDocsAuditLogHandler.handle(callback);

        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList)
            .extracting(ILoggingEvent::getFormattedMessage, ILoggingEvent::getLevel)
            .containsExactly(
                Tuple.tuple("Edit Document audit logs...", Level.INFO),
                Tuple.tuple("CCD case id: 1234", Level.INFO),
                Tuple.tuple("Delete/Update document ids: [id1, id2, id3, id4, id5, id6, id7, id8, id9]", Level.INFO),
                Tuple.tuple("IDAM User id: user-details-124", Level.INFO));
    }

    private void mockAuditServiceDependencies() {
        when(editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(any(AsylumCase.class),
            any(AsylumCase.class), any(AsylumCaseFieldDefinition.class)))
            .thenReturn(Collections.singletonList("id1"))
            .thenReturn(Collections.singletonList("id2"))
            .thenReturn(Collections.singletonList("id3"))
            .thenReturn(Collections.singletonList("id4"))
            .thenReturn(Collections.singletonList("id5"))
            .thenReturn(Collections.singletonList("id6"))
            .thenReturn(Collections.singletonList("id7"))
            .thenReturn(Collections.singletonList("id8"))
            .thenReturn(Collections.singletonList("id9"))
            .thenThrow(new RuntimeException("no more calls expected"));
    }

    private void mockUserDetailsProvider() {
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn("user-details-124");
    }

    private void mockCallbackAndCaseDetails() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(1234L);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);
    }
}