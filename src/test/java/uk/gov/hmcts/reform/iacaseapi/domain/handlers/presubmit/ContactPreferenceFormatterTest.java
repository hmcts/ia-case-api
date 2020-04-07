package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class ContactPreferenceFormatterTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Captor private ArgumentCaptor<Object> subscriberCaptor;
    @Captor private ArgumentCaptor<AsylumCaseFieldDefinition> asylumExtractorCaptor;

    private ContactPreferenceFormatter contactPreferenceFormatter = new ContactPreferenceFormatter();

    @Test
    public void should_format_contact_preference() {
        final List<IdValue<Subscriber>> subscriptions =
            Arrays.asList(
                new IdValue<>("1", new Subscriber(
                    SubscriberType.APPELLANT,
                    "Optional[ia@gmail.com]",
                    YesOrNo.YES,
                    "",
                    YesOrNo.NO))
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.CONTACT_PREFERENCE, ContactPreference.class)).thenReturn(Optional.of(ContactPreference.WANTS_EMAIL));
        when(asylumCase.read(EMAIL)).thenReturn(Optional.of("ia@gmail.com"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            contactPreferenceFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(asylumExtractorCaptor.capture(), subscriberCaptor.capture());

        List<IdValue<Subscriber>> actualSubscriptions = (List<IdValue<Subscriber>>) subscriberCaptor.getAllValues().get(0);

        assertEquals(
            SUBSCRIPTIONS,
            asylumExtractorCaptor.getAllValues().get(0)
        );

        assertEquals(
            subscriptions.size(),
            actualSubscriptions.size()
        );

        assertEquals(subscriptions.get(0).getValue().getSubscriber(), actualSubscriptions.get(0).getValue().getSubscriber());
        assertEquals(subscriptions.get(0).getValue().getEmail(), actualSubscriptions.get(0).getValue().getEmail());
        assertEquals(subscriptions.get(0).getValue().getWantsEmail(), actualSubscriptions.get(0).getValue().getWantsEmail());
        assertEquals(subscriptions.get(0).getValue().getMobileNumber(), actualSubscriptions.get(0).getValue().getMobileNumber());
        assertEquals(subscriptions.get(0).getValue().getWantsSms(), actualSubscriptions.get(0).getValue().getWantsSms());
    }
}
