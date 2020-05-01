package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.beust.jcommander.internal.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionDecision.GRANTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionStatus.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.AWAITING_REASONS_FOR_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class ReviewTimeExtensionsHandlerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private ReviewTimeExtensionsHandler reviewTimeExtensionHandler;

    @Before
    public void setup() {
        reviewTimeExtensionHandler =
            new ReviewTimeExtensionsHandler();
    }

    @Test
    public void handles_a_refused_extension() {
        IdValue<TimeExtension> extensionIdValue1 = new IdValue<>("1", new TimeExtension(null, "reasons1", State.APPEAL_SUBMITTED, IN_PROGRESS, emptyList()));
        IdValue<TimeExtension> extensionIdValue2 = new IdValue<>("2", new TimeExtension("date2", "reasons2", AWAITING_REASONS_FOR_APPEAL, SUBMITTED, emptyList()));
        List<IdValue<TimeExtension>> timeExtensions = asList(extensionIdValue1, extensionIdValue2);

        List<IdValue<Direction>> existingDirections =
            Arrays.asList(
                new IdValue<>("1", new Direction(
                    "explanation-1",
                    Parties.LEGAL_REPRESENTATIVE,
                    "2019-07-07",
                    "2019-12-01",
                    DirectionTag.REQUEST_REASONS_FOR_APPEAL,
                    Collections.emptyList()
                )),
                new IdValue<>("2", new Direction(
                    "explanation-2",
                    Parties.RESPONDENT,
                    "2020-11-01",
                    "2019-11-01",
                    DirectionTag.RESPONDENT_REVIEW,
                    newArrayList(new IdValue<>("1", new PreviousDates("2018-05-01", "2018-03-01")))
                ))
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REVIEW_TIME_EXTENSION);
        when(caseDetails.getState()).thenReturn(AWAITING_REASONS_FOR_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getCaseData().read(DIRECTIONS)).thenReturn(Optional.of(existingDirections));
        when(asylumCase.read(TIME_EXTENSIONS)).thenReturn(Optional.of(timeExtensions));
        when(asylumCase.read(REVIEW_TIME_EXTENSION_DECISION)).thenReturn(Optional.of(TimeExtensionDecision.REFUSED));
        when(asylumCase.read(REVIEW_TIME_EXTENSION_DECISION_REASON)).thenReturn(Optional.of("Decision reason"));
        when(asylumCase.read(REVIEW_TIME_EXTENSION_DUE_DATE)).thenReturn(Optional.of("2019-07-07"));

        reviewTimeExtensionHandler.handle(ABOUT_TO_SUBMIT, callback);

        IdValue<TimeExtension> refusedExtension = new IdValue<>("2", new TimeExtension("date2", "reasons2", AWAITING_REASONS_FOR_APPEAL, REFUSED, emptyList(), TimeExtensionDecision.REFUSED, "Decision reason", "2019-07-07"));
        Mockito.verify(asylumCase).write(TIME_EXTENSIONS, asList(extensionIdValue1, refusedExtension));
        Mockito.verify(asylumCase).write(REVIEW_TIME_EXTENSION_REQUIRED, YesOrNo.NO);
    }

    @Test
    public void handles_a_granted_extension() {
        IdValue<TimeExtension> extensionIdValue1 = new IdValue<>("1", new TimeExtension(null, "reasons1", State.APPEAL_SUBMITTED, IN_PROGRESS, emptyList()));
        IdValue<TimeExtension> extensionIdValue2 = new IdValue<>("2", new TimeExtension("date2", "reasons2", AWAITING_REASONS_FOR_APPEAL, SUBMITTED, emptyList()));
        List<IdValue<TimeExtension>> timeExtensions = asList(extensionIdValue1, extensionIdValue2);

        List<IdValue<Direction>> existingDirections =
            Arrays.asList(
                new IdValue<>("1", new Direction(
                    "explanation-1",
                    Parties.LEGAL_REPRESENTATIVE,
                    "2019-07-07",
                    "2019-12-01",
                    DirectionTag.REQUEST_REASONS_FOR_APPEAL,
                    Collections.emptyList()
                )),
                new IdValue<>("2", new Direction(
                    "explanation-2",
                    Parties.RESPONDENT,
                    "2020-11-01",
                    "2019-11-01",
                    DirectionTag.RESPONDENT_REVIEW,
                    newArrayList(new IdValue<>("1", new PreviousDates("2018-05-01", "2018-03-01")))
                ))
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REVIEW_TIME_EXTENSION);
        when(caseDetails.getState()).thenReturn(AWAITING_REASONS_FOR_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(TIME_EXTENSIONS)).thenReturn(Optional.of(timeExtensions));
        when(caseDetails.getCaseData().read(DIRECTIONS)).thenReturn(Optional.of(existingDirections));
        when(asylumCase.read(REVIEW_TIME_EXTENSION_DECISION)).thenReturn(Optional.of(GRANTED));
        when(asylumCase.read(REVIEW_TIME_EXTENSION_DECISION_REASON)).thenReturn(Optional.of("Decision reason"));
        when(asylumCase.read(REVIEW_TIME_EXTENSION_DUE_DATE)).thenReturn(Optional.of("2020-06-15"));


        reviewTimeExtensionHandler.handle(ABOUT_TO_SUBMIT, callback);

        IdValue<TimeExtension> grantedExtension = new IdValue<>("2", new TimeExtension("date2", "reasons2", AWAITING_REASONS_FOR_APPEAL, TimeExtensionStatus.GRANTED, emptyList(), GRANTED, "Decision reason", "2020-06-15"));
        Mockito.verify(asylumCase).write(TIME_EXTENSIONS, asList(extensionIdValue1, grantedExtension));
        Mockito.verify(asylumCase).write(REVIEW_TIME_EXTENSION_REQUIRED, YesOrNo.NO);
    }

    @Test
    public void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = reviewTimeExtensionHandler.canHandle(callbackStage, callback);

                if (event == Event.REVIEW_TIME_EXTENSION && callbackStage == ABOUT_TO_SUBMIT) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> reviewTimeExtensionHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewTimeExtensionHandler.canHandle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewTimeExtensionHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewTimeExtensionHandler.handle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
