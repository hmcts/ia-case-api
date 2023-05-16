package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTION_PARTIES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionPartiesProvider;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SendDirectionPreparerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DirectionPartiesProvider directionPartiesProvider;

    @InjectMocks
    private SendDirectionPreparer sendDirectionPreparer;

    @BeforeEach
    public void setup() {
        sendDirectionPreparer = new SendDirectionPreparer(directionPartiesProvider);
    }

    @Test
    void should_prepare_roles_for_direction_parties_list() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        DynamicList dynamicList;
        final List<Value> values = Arrays.asList(
                new Value(LEGAL_REPRESENTATIVE.name(), LEGAL_REPRESENTATIVE.toString()),
                new Value(RESPONDENT.name(), RESPONDENT.toString()),
                new Value(BOTH.name(), BOTH.toString()),
                new Value(APPELLANT.name(), APPELLANT.toString()));

        dynamicList = new DynamicList(values.get(0), values);

        when(directionPartiesProvider.getDirectionParties(callback)).thenReturn(dynamicList);

        sendDirectionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, times(1)).write(DIRECTION_PARTIES, dynamicList);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION_WITH_QUESTIONS);
        assertThatThrownBy(
                () -> sendDirectionPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = sendDirectionPreparer.canHandle(callbackStage, callback);

                if (event == Event.SEND_DIRECTION
                        && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }
}





