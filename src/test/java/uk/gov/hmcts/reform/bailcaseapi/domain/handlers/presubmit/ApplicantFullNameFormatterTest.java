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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FULL_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class ApplicantFullNameFormatterTest {

    @Mock
    private Callback<BailCase> callback;

    @Mock
    private BailCase bailCase;

    @Mock
    private CaseDetails<BailCase> caseDetails;

    private ApplicantFullNameFormatter applicantFullNameFormatter;

    @BeforeEach
    public void setUp() {
        applicantFullNameFormatter = new ApplicantFullNameFormatter();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
    }

    @Test
    void should_concatenate_given_names_and_family_name() {

        Map<Pair<String, String>, String> exampleInputOutputs =
            ImmutableMap
                .of(Pair.of("Max Anthony", "Smith"), "Max Anthony Smith",
                    Pair.of("John", "Doe"), "John Doe",
                    Pair.of(" Mary ", " Bell "), "Mary Bell"
                );

        exampleInputOutputs
            .forEach((input, output) -> {

                when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(input.getKey()));
                when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(input.getValue()));

                PreSubmitCallbackResponse<BailCase> callbackResponse =
                    applicantFullNameFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

                assertNotNull(callbackResponse);
                assertThat(callbackResponse.getData()).isNotEmpty();
                assertEquals(bailCase, callbackResponse.getData());
                verify(bailCase, times(1)).write(APPLICANT_FULL_NAME, output);

                reset(bailCase);
            });

    }

    @Test
    void should_throw_when_applicant_given_names_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> applicantFullNameFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("applicantGivenNames is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_applicant_family_name_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("John"));
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> applicantFullNameFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("applicantFamilyName is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = applicantFullNameFormatter.canHandle(callbackStage, callback);
                if (callbackStage == ABOUT_TO_SUBMIT
                    && (callback.getEvent() == Event.START_APPLICATION
                        || callback.getEvent() == Event.EDIT_BAIL_APPLICATION
                        || callback.getEvent() == Event.MAKE_NEW_APPLICATION
                        || callback.getEvent() == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {
        //invalid stage
        assertThatThrownBy(() -> applicantFullNameFormatter.handle(ABOUT_TO_START, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);

        //invalid event
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION);
        assertThatThrownBy(() -> applicantFullNameFormatter.handle(ABOUT_TO_SUBMIT, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> applicantFullNameFormatter.canHandle(null, callback)).hasMessage(
            "callbackStage must not be null").isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applicantFullNameFormatter.canHandle(ABOUT_TO_SUBMIT, null)).hasMessage(
            "callback must not be null").isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applicantFullNameFormatter.handle(null, callback)).hasMessage(
            "callbackStage must not be null").isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applicantFullNameFormatter.handle(ABOUT_TO_SUBMIT, null)).hasMessage(
            "callback must not be null").isExactlyInstanceOf(NullPointerException.class);
    }

}
