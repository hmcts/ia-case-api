package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.OrganisationPolicy;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class GeneratePinInPostPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private OrganisationPolicy organisationPolicy;

    private ChangeOrganisationRequest changeOrganisationRequest;
    private GeneratePinInPostPreparer generatePinInPostPreparer;

    @BeforeEach
    public void setUp() {
        generatePinInPostPreparer =
            new GeneratePinInPostPreparer();
    }

    @Test
    void can_handle_true() {
        when(callback.getEvent()).thenReturn(Event.GENERATE_PIN_IN_POST);
        assertTrue(generatePinInPostPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, mode = EnumSource.Mode.EXCLUDE, names = {"ABOUT_TO_START"})
    void can_handle_false_for_non_about_to_start(PreSubmitCallbackStage callbackStage) {
        assertFalse(generatePinInPostPreparer.canHandle(callbackStage, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, mode = EnumSource.Mode.EXCLUDE, names = {"GENERATE_PIN_IN_POST"})
    void can_handle_false_for_other_events(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertFalse(generatePinInPostPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> generatePinInPostPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> generatePinInPostPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generatePinInPostPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generatePinInPostPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void throws_if_cannot_handle_callback() {
        assertThatThrownBy(() -> generatePinInPostPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = AsylumCaseFieldDefinition.class, names = {
        "LEGAL_REP_NAME",
        "LEGAL_REPRESENTATIVE_NAME",
        "LEGAL_REPRESENTATIVE_EMAIL_ADDRESS",
        "LEGAL_REP_COMPANY",
        "LEGAL_REP_COMPANY_NAME",
        "LEGAL_REP_REFERENCE_NUMBER",
        "LEGAL_REP_INDIVIDUAL_PARTY_ID",
        "LEGAL_REP_ORGANISATION_PARTY_ID"
    })
    void errors_if_represented(AsylumCaseFieldDefinition fieldDefinition) {
        when(callback.getEvent()).thenReturn(Event.GENERATE_PIN_IN_POST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(any(AsylumCaseFieldDefinition.class), eq(String.class))).thenReturn(Optional.empty());
        when(asylumCase.read(fieldDefinition, String.class))
            .thenReturn(Optional.of("something"));
        PreSubmitCallbackResponse<AsylumCase> response = generatePinInPostPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
        assertTrue(response.getErrors()
            .contains("Case still has a legal representative, cannot generate PIN in post. " +
                "Please run Remove Legal Representative event to generate."));
    }

    @Test
    void does_not_error_if_not_represented() {
        when(callback.getEvent()).thenReturn(Event.GENERATE_PIN_IN_POST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        PreSubmitCallbackResponse<AsylumCase> response = generatePinInPostPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
        assertTrue(response.getErrors().isEmpty());
    }
}
