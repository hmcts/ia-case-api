package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.MakeAnApplicationTypesProvider;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class MakeAnApplicationPreparerTest {

    @Mock private Callback callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Mock private FeatureToggler featureToggler;

    @Mock private MakeAnApplicationTypesProvider makeAnApplicationTypesProvider;

    @InjectMocks
    public MakeAnApplicationPreparer makeAnApplicationPreparer;

    @Before
    public void setUp() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MAKE_AN_APPLICATION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("make-an-application-feature", false)).thenReturn(true);
    }

    @Test
    public void should_return_valid_make_an_application_types() {

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value("updateAppealDetails", UPDATE_APPEAL_DETAILS.toString()),
            new Value("withdraw", WITHDRAW.toString()),
            new Value("other", OTHER.toString()));
        DynamicList dynamicList =
            new DynamicList(values.get(0), values);

        when(makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback))
            .thenReturn(dynamicList);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            makeAnApplicationPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
            .write(AsylumCaseFieldDefinition.MAKE_AN_APPLICATION_TYPES, makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback));

    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> makeAnApplicationPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> makeAnApplicationPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> makeAnApplicationPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> makeAnApplicationPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> makeAnApplicationPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = makeAnApplicationPreparer.canHandle(callbackStage, callback);

                if ((event == Event.MAKE_AN_APPLICATION)
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
