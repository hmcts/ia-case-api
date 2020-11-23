package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority.EARLIEST;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DataFixer;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class AsylumCaseDataFixingHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DataFixer dataFixer1;
    @Mock
    private DataFixer dataFixer2;

    private List<DataFixer> dataFixers;

    private AsylumCaseDataFixingHandler asylumCaseDataFixingHandler;

    @BeforeEach
    public void setUp() {

        dataFixers = asList(dataFixer1, dataFixer2);

        asylumCaseDataFixingHandler = new AsylumCaseDataFixingHandler(dataFixers);
    }

    @Test
    public void set_to_earliest() {
        assertThat(asylumCaseDataFixingHandler.getDispatchPriority()).isEqualTo(EARLIEST);
    }

    @Test
    public void calls_all_fixers() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        asylumCaseDataFixingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        dataFixers.forEach(df -> verify(df, times(1)).fix(asylumCase));
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = asylumCaseDataFixingHandler.canHandle(callbackStage, callback);

                assertThat(canHandle).isEqualTo(true);
            }
        }

        reset(callback);
    }
}
