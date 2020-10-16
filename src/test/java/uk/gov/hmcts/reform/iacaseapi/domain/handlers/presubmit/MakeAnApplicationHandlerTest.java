package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;

import java.util.*;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.MakeAnApplicationAppender;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class MakeAnApplicationHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Mock private FeatureToggler featureToggler;

    @Mock private MakeAnApplicationAppender makeAnApplicationAppender;

    @InjectMocks
    private MakeAnApplicationHandler makeAnApplicationHandler;

    @Before
    public void setUp() {

        MockitoAnnotations.openMocks(this);

        makeAnApplicationHandler = new MakeAnApplicationHandler(makeAnApplicationAppender, featureToggler);
        when(featureToggler.getValue("make-an-application-feature", false)).thenReturn(true);
    }

    @Test
    public void should_append_make_an_application() {

        final List<IdValue<MakeAnApplication>> existingMakeAnApplications = new ArrayList<>();
        final List<IdValue<MakeAnApplication>> newMakeAnApplications = new ArrayList<>();

        DynamicList makeAnApplicationTypes = new DynamicList("updateAppealDetails");

        String newMakeAnApplicationType = makeAnApplicationTypes.getValue().getLabel();
        String newMakeAnApplicationReason = "Some reason";
        List<IdValue<Document>> newMakeAnApplicationEvidence = Collections.emptyList();
        String newMakeAnApplicationStatus = "Pending";
        String currentState = "listing";

        when(callback.getEvent()).thenReturn(MAKE_AN_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);


        when(callback.getCaseDetails().getState()).thenReturn(State.LISTING);
        when(asylumCase.read(MAKE_AN_APPLICATION_TYPES, DynamicList.class))
            .thenReturn(Optional.of(makeAnApplicationTypes));
        when(asylumCase.read(MAKE_AN_APPLICATION_DETAILS, String.class)).thenReturn(Optional.of(newMakeAnApplicationReason));
        when(asylumCase.read(MAKE_AN_APPLICATION_EVIDENCE)).thenReturn(Optional.of(newMakeAnApplicationEvidence));
        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(existingMakeAnApplications));

        when(makeAnApplicationAppender.append(
            existingMakeAnApplications,
            newMakeAnApplicationType,
            newMakeAnApplicationReason,
            newMakeAnApplicationEvidence,
            newMakeAnApplicationStatus,
            currentState)).thenReturn(newMakeAnApplications);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            makeAnApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
            .read(MAKE_AN_APPLICATION_TYPES, DynamicList.class);
        verify(asylumCase, times(1))
            .read(MAKE_AN_APPLICATION_DETAILS, String.class);
        verify(asylumCase, times(1))
            .read(MAKE_AN_APPLICATION_EVIDENCE);
        verify(asylumCase, times(1))
            .read(MAKE_AN_APPLICATIONS);

        verify(makeAnApplicationAppender, times(1))
            .append(existingMakeAnApplications, newMakeAnApplicationType,
                newMakeAnApplicationReason, newMakeAnApplicationEvidence,
                newMakeAnApplicationStatus, currentState);

        verify(asylumCase, times(1)).write(MAKE_AN_APPLICATIONS, newMakeAnApplications);
        verify(asylumCase, times(1)).clear(MAKE_AN_APPLICATION_TYPES);
        verify(asylumCase, times(1)).clear(MAKE_AN_APPLICATION_DETAILS);
        verify(asylumCase, times(1)).clear(MAKE_AN_APPLICATION_EVIDENCE);
        verify(asylumCase, times(1)).clear(MAKE_AN_APPLICATION_DETAILS_LABEL);
    }

    @Test
    public void should_throw_on_missing_make_an_application_type() {

        when(callback.getEvent()).thenReturn(MAKE_AN_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> makeAnApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("MakeAnApplication type is not present");
    }

    @Test
    public void should_throw_on_missing_make_an_application_reason() {

        when(callback.getEvent()).thenReturn(MAKE_AN_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(MAKE_AN_APPLICATION_TYPES, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList("updateAppealDetails")));

        Assertions.assertThatThrownBy(() -> makeAnApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("MakeAnApplication details is not present");
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> makeAnApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> makeAnApplicationHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> makeAnApplicationHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> makeAnApplicationHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> makeAnApplicationHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> makeAnApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = makeAnApplicationHandler.canHandle(callbackStage, callback);

                if ((event == Event.MAKE_AN_APPLICATION)
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }
}
