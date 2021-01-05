package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATIONS_LIST;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DecideAnApplicationPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Mock
    private DateProvider dateProvider;

    private DecideAnApplicationPreparer decideAnApplicationPreparer;

    @BeforeEach
    public void setUp() {

        decideAnApplicationPreparer = new DecideAnApplicationPreparer();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.DECIDE_AN_APPLICATION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_handle_the_about_to_start() {

        when(dateProvider.now()).thenReturn(LocalDate.MAX);

        List<IdValue<Document>> evidence =
            Arrays.asList(new IdValue<>("1", new Document("url", "url", "FileName")));
        MakeAnApplication makeAnApplication =
            new MakeAnApplication("Legal representative", "Update appeal details", "A reason to update appeal details",
                evidence, dateProvider.now().toString(), "Pending",
                State.LISTING.toString());
        makeAnApplication.setApplicantRole("caseworker-ia-caseofficer");
        final List<IdValue<MakeAnApplication>> makeAnApplications =
            Arrays.asList(new IdValue<>("1", makeAnApplication));

        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decideAnApplicationPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
            .write(MAKE_AN_APPLICATIONS_LIST,
                new DynamicList(new Value("1", "Legal representative : Application 1"),
                    Arrays.asList(new Value("1", "Legal representative : Application 1"))));
    }

    @Test
    void should_return_error_if_no_applications_to_decide() {

        final List<IdValue<MakeAnApplication>> makeAnApplications = Collections.emptyList();
        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decideAnApplicationPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).contains("There are no applications to decide.");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> decideAnApplicationPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> decideAnApplicationPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideAnApplicationPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideAnApplicationPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideAnApplicationPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = decideAnApplicationPreparer.canHandle(callbackStage, callback);

                if ((event == Event.DECIDE_AN_APPLICATION)
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
