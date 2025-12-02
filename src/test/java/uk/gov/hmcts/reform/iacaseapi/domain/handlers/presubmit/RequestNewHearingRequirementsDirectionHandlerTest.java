package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PreviousHearingAppender;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre.DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre.NEWPORT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RequestNewHearingRequirementsDirectionHandlerTest {

    private static final int HEARING_REQUIREMENTS_DUE_IN_DAYS = 5;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private DirectionAppender directionAppender;
    @Mock
    private PreviousHearingAppender previousHearingAppender;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private DocumentWithMetadata hearingRequirements1;
    private RequestNewHearingRequirementsDirectionHandler requestNewHearingRequirementsDirectionHandler;

    @BeforeEach
    public void setUp() {
        requestNewHearingRequirementsDirectionHandler =
                new RequestNewHearingRequirementsDirectionHandler(
                        HEARING_REQUIREMENTS_DUE_IN_DAYS,
                        dateProvider,
                        directionAppender,
                        previousHearingAppender,
                        featureToggler
                );
    }

    @Test
    void can_handle_request_new_hearing_requirements() {

        when(callback.getEvent()).thenReturn(Event.REQUEST_NEW_HEARING_REQUIREMENTS);
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);

        requestNewHearingRequirementsDirectionHandler =
                new RequestNewHearingRequirementsDirectionHandler(
                        HEARING_REQUIREMENTS_DUE_IN_DAYS,
                        dateProvider,
                        directionAppender,
                        previousHearingAppender,
                        featureToggler
                );

        boolean canHandle =
                requestNewHearingRequirementsDirectionHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertTrue(canHandle);
    }

    static Stream<Arguments> hearingCentersScenarios() {
        return Stream.of(
                Arguments.of(YES, NEWPORT, YES),
                Arguments.of(YES, NEWPORT, NO),
                Arguments.of(NO, DECISION_WITHOUT_HEARING, YES),
                Arguments.of(NO, DECISION_WITHOUT_HEARING, NO),
                Arguments.of(NO, null, NO),
                Arguments.of(YES, null, YES)
        );
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> requestNewHearingRequirementsDirectionHandler
                .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> requestNewHearingRequirementsDirectionHandler
                .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = requestNewHearingRequirementsDirectionHandler.canHandle(callbackStage, callback);

                if (event == Event.REQUEST_NEW_HEARING_REQUIREMENTS
                        && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(featureToggler.getValue("reheard-feature", false)).thenReturn(false);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = requestNewHearingRequirementsDirectionHandler.canHandle(callbackStage, callback);

                if (event == Event.REQUEST_NEW_HEARING_REQUIREMENTS
                        && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> requestNewHearingRequirementsDirectionHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
                () -> requestNewHearingRequirementsDirectionHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestNewHearingRequirementsDirectionHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
                () -> requestNewHearingRequirementsDirectionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }
}