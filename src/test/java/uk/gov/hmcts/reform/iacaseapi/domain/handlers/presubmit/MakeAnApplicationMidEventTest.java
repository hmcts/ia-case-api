package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.*;

import java.util.*;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.MakeAnApplicationTypesProvider;

@RunWith(JUnitParamsRunner.class)
@SuppressWarnings("unchecked")
public class MakeAnApplicationMidEventTest {

    @Mock private Callback callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Mock private FeatureToggler featureToggler;

    @Mock private MakeAnApplicationTypesProvider makeAnApplicationTypesProvider;

    @InjectMocks
    private MakeAnApplicationMidEvent makeAnApplicationMidEvent;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MAKE_AN_APPLICATION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("make-an-application-feature", false)).thenReturn(true);
    }

    @Test
    @Parameters({
        "ADJOURN",
        "EXPEDITE",
        "TIME_EXTENSION",
        "TRANSFER",
        "UPDATE_APPEAL_DETAILS",
        "UPDATE_HEARING_REQUIREMENTS",
        "LINK_OR_UNLINK",
        "JUDGE_REVIEW",
        "REINSTATE",
        "WITHDRAW",
        "OTHER"
    })
    public void should_return_valid_make_an_application_types(String type) {

        final List<Value> values = new ArrayList<>();
        Collections.addAll(values,
            new Value(ADJOURN.name(), ADJOURN.toString()),
            new Value(EXPEDITE.name(), EXPEDITE.toString()),
            new Value(TRANSFER.name(), TRANSFER.toString()),
            new Value(UPDATE_APPEAL_DETAILS.name(), UPDATE_APPEAL_DETAILS.toString()),
            new Value(UPDATE_HEARING_REQUIREMENTS.name(), UPDATE_HEARING_REQUIREMENTS.toString()),
            new Value(TIME_EXTENSION.name(), TIME_EXTENSION.toString()),
            new Value(WITHDRAW.name(), WITHDRAW.toString()),
            new Value(OTHER.name(), OTHER.toString()));
        DynamicList makeAnApplicationTypes =
            new DynamicList(values.get(0), values);

        DynamicList selectedMakeAnApplicationTypes =
            new DynamicList(
                new Value(type, MakeAnApplicationTypes.valueOf(type).toString()),
                Arrays.asList(new Value(type, MakeAnApplicationTypes.valueOf(type).toString()))
            );

        when(asylumCase.read(MAKE_AN_APPLICATION_TYPES, DynamicList.class))
            .thenReturn(Optional.of(selectedMakeAnApplicationTypes));
        when(makeAnApplicationTypesProvider.getMakeAnApplicationTypes(callback))
            .thenReturn(makeAnApplicationTypes);

        DynamicList allMakeAnApplicationTypes =
            new DynamicList(selectedMakeAnApplicationTypes.getValue(), makeAnApplicationTypes.getListItems());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            makeAnApplicationMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
            .write(MAKE_AN_APPLICATION_TYPES, allMakeAnApplicationTypes);

        switch (MakeAnApplicationTypes.valueOf(type)) {
            case ADJOURN:
                verify(asylumCase, times(1))
                    .write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                        "Explain why you need an adjournment and for how long you need it.");
                break;
            case EXPEDITE:
                verify(asylumCase, times(1))
                    .write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                        "Explain why you need to expedite the appeal. Include the latest date you would like the appeal to be decided "
                        + "by and state if you are willing for the appeal to be decided without a hearing.");
                break;
            case LINK_OR_UNLINK:
                verify(asylumCase, times(1))
                    .write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                        "Explain why you want to link or unlink this appeal. You must include the appellant name and HMCTS appeal "
                        + "reference of each appeal you want to link to or unlink from.");
                break;
            case JUDGE_REVIEW:
                verify(asylumCase, times(1))
                    .write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                        "Tell us which application decision you want to be reviewed by a Judge and explain why you think the "
                        + "original decision was wrong.");
                break;
            case REINSTATE:
                verify(asylumCase, times(1))
                    .write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                        "Explain why you believe the Tribunal should reinstate this appeal.");
                break;
            case TIME_EXTENSION:
                verify(asylumCase, times(1))
                    .write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                        "Tell us which task you need more time to complete, explain why you need more time and include "
                        + "how much more time you will need."
                    );
                break;
            case TRANSFER:
                verify(asylumCase, times(1))
                    .write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                        "Tell us which hearing centre you want to transfer the appeal to and why.");
                break;
            case UPDATE_APPEAL_DETAILS:
                verify(asylumCase, times(1))
                    .write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                        "Tell us which appeal details you want to update and explain why the changes are necessary.");
                break;
            case UPDATE_HEARING_REQUIREMENTS:
                verify(asylumCase, times(1))
                    .write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                        "Tell us which hearing requirements you want to update and explain why the changes are necessary.");
                break;
            case WITHDRAW:
                verify(asylumCase, times(1))
                    .write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                        "Explain why you want to withdraw the appeal.");
                break;
            case OTHER:
                verify(asylumCase, times(1))
                    .write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                        "Describe the application you are making and explain the reasons for the application.");
                break;
            default:
                break;
        }
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> makeAnApplicationMidEvent.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> makeAnApplicationMidEvent.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> makeAnApplicationMidEvent.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> makeAnApplicationMidEvent.canHandle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> makeAnApplicationMidEvent.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> makeAnApplicationMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = makeAnApplicationMidEvent.canHandle(callbackStage, callback);

                if ((event == Event.MAKE_AN_APPLICATION)
                    && callbackStage == PreSubmitCallbackStage.MID_EVENT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }
}
