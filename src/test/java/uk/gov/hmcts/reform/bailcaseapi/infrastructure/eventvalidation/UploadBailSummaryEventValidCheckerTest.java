package uk.gov.hmcts.reform.bailcaseapi.infrastructure.eventvalidation;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.util.LoggerUtil;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation.UploadBailSummaryEventValidChecker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_BAIL_SUMMARY_ACTION_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPLOAD_BAIL_SUMMARY;

@ExtendWith(MockitoExtension.class)
class UploadBailSummaryEventValidCheckerTest {
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    private UploadBailSummaryEventValidChecker uploadBailSummaryEventValidChecker =
        new UploadBailSummaryEventValidChecker();

    private ListAppender<ILoggingEvent> loggingEventListAppender;

    @BeforeEach
    public void setUp() {

    }

    @Test
    void canSendValidEvents() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(UPLOAD_BAIL_SUMMARY);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(caseDetails.getState()).thenReturn(State.BAIL_SUMMARY_UPLOADED);
        when(bailCase.read(UPLOAD_BAIL_SUMMARY_ACTION_AVAILABLE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        EventValid check = uploadBailSummaryEventValidChecker.check(callback);

        assertThat(check.isValid()).isTrue();
    }

    @Test
    void cannotSendInvalidEvents() {

        loggingEventListAppender = LoggerUtil.getListAppenderForClass(UploadBailSummaryEventValidChecker.class);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(UPLOAD_BAIL_SUMMARY);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(caseDetails.getState()).thenReturn(State.BAIL_SUMMARY_UPLOADED);
        when(bailCase.read(UPLOAD_BAIL_SUMMARY_ACTION_AVAILABLE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        EventValid check = uploadBailSummaryEventValidChecker.check(callback);

        assertThat(check.isValid()).isFalse();
        assertThat(check.getInvalidReason()).contains("Bail Summary has already been uploaded to this case.");

        Assertions.assertThat(loggingEventListAppender.list)
            .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
            .contains(Tuple.tuple("Bail Summary has already been uploaded to this case.", Level.ERROR));
    }

}
