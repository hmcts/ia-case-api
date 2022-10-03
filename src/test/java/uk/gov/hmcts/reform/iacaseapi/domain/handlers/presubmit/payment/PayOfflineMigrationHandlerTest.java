package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PayOfflineMigrationHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private PayOfflineMigrationHandler payOfflineMigrationHandler;

    @BeforeEach
    public void setUp() {
        payOfflineMigrationHandler = new PayOfflineMigrationHandler();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(State.APPEAL_STARTED);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class)
    void should_overwrite_payOffline_as_payLater(Event event) {

        when(callback.getEvent()).thenReturn(event);

        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.PA));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));

        payOfflineMigrationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(PA_APPEAL_TYPE_PAYMENT_OPTION, "payLater");
    }

    @ParameterizedTest
    @EnumSource(value = Event.class)
    void should_not_overwrite_other_values() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        asylumCase.write(APPEAL_TYPE, Optional.of(AppealType.PA));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payNow"));

        payOfflineMigrationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, never()).write(PA_APPEAL_TYPE_PAYMENT_OPTION, "payLater");
    }
}
