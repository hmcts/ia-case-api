package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_APPELLANT_MINOR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MinorTagHandlerTest {

    @Mock private
    Callback<AsylumCase> callback;
    @Mock private
    CaseDetails<AsylumCase> caseDetails;

    final MinorTagHandler minorTagHandler = new MinorTagHandler();
    static final String APPELLANT_ADULT = LocalDate.of(1979, 2, 1).toString();
    static final String APPELLANT_MINOR = LocalDate.now().toString();

    @ParameterizedTest
    @MethodSource("generateCanHandleTestScenario")
    void it_can_handle_callback(PreSubmitCallbackStage callbackStage, Event event, boolean canHandledExpected) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        boolean canHandleActual = minorTagHandler.canHandle(callbackStage, callback);

        assertThat(canHandleActual).isEqualTo(canHandledExpected);
    }

    private static Stream<Arguments> generateCanHandleTestScenario() {

        List<Arguments> scenarios = new ArrayList<>();
        List<Event> validEvents = Arrays.asList(SUBMIT_APPEAL, EDIT_APPEAL_AFTER_SUBMIT);
        for (Event event : Event.values()) {
            if (validEvents.contains(event)) {
                for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                    if (callbackStage.equals(ABOUT_TO_SUBMIT)) {
                        scenarios.add(Arguments.of(ABOUT_TO_SUBMIT, event, true));
                    } else {
                        scenarios.add(Arguments.of(callbackStage, event, false));
                    }
                }
            } else {
                for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                    scenarios.add(Arguments.of(callbackStage, event, false));
                }
            }
        }

        return scenarios.stream();
    }

    @ParameterizedTest
    @MethodSource("generateAppellantDobScenarios")
    void given_appellant_dob_should_tag_case_as_minor_or_not_accordingly(String appellantDob, Event event, YesOrNo isAppellantMinorExpected) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(APPELLANT_DATE_OF_BIRTH, appellantDob);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> actual = minorTagHandler.handle(ABOUT_TO_SUBMIT, callback);

        YesOrNo isAppellantMinor = actual.getData().read(IS_APPELLANT_MINOR, YesOrNo.class)
            .orElse(null);
        assertThat(isAppellantMinor).isEqualTo(isAppellantMinorExpected);
    }

    private static Stream<Arguments> generateAppellantDobScenarios() {

        List<Arguments> scenarios = new ArrayList<>();
        scenarios.add(Arguments.of(APPELLANT_MINOR, SUBMIT_APPEAL, YesOrNo.YES));
        scenarios.add(Arguments.of(APPELLANT_ADULT, SUBMIT_APPEAL, YesOrNo.NO));
        scenarios.add(Arguments.of(APPELLANT_MINOR, EDIT_APPEAL_AFTER_SUBMIT, YesOrNo.YES));
        scenarios.add(Arguments.of(APPELLANT_ADULT, EDIT_APPEAL_AFTER_SUBMIT, YesOrNo.NO));

        return scenarios.stream();
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> minorTagHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> minorTagHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> minorTagHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> minorTagHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("wrongEventAndCallbackTestData")
    void given_wrong_event_or_wrong_callback_should_throw_exception(
        PreSubmitCallbackStage callbackStage,
        Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(APPELLANT_DATE_OF_BIRTH, APPELLANT_ADULT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> minorTagHandler.handle(callbackStage, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    private static Stream<Arguments> wrongEventAndCallbackTestData() {

        return Stream.of(
            Arguments.of(ABOUT_TO_START, SUBMIT_APPEAL),
            Arguments.of(ABOUT_TO_START, EDIT_APPEAL_AFTER_SUBMIT),
            Arguments.of(ABOUT_TO_SUBMIT, START_APPEAL)
        );
    }

}