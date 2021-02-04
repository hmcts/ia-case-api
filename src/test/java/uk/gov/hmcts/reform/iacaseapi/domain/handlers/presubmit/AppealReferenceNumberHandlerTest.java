package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority.EARLIEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.Organisation;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.OrganisationPolicy;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AppealReferenceNumberHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Mock
    private DateProvider dateProvider;
    @Mock
    private AppealReferenceNumberGenerator appealReferenceNumberGenerator;
    @Mock
    private FeatureToggler featureToggler;

    private AppealReferenceNumberHandler appealReferenceNumberHandler;

    private String organisationIdentifier = "0UFUG4Z";

    private OrganisationPolicy organisationPolicy;

    @BeforeEach
    public void setUp() {

        appealReferenceNumberHandler =
            new AppealReferenceNumberHandler(dateProvider, appealReferenceNumberGenerator, featureToggler);

        organisationPolicy =
            OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                    .organisationID(organisationIdentifier)
                    .build()
                )
                .orgPolicyReference("Some reference")
                .orgPolicyCaseAssignedRole("caseworker-ia-legalrep-solicitor")
                .build();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(123L);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("share-case-feature", false)).thenReturn(true);
    }

    @Test
    void set_to_earliest() {
        assertThat(appealReferenceNumberHandler.getDispatchPriority()).isEqualTo(EARLIEST);
    }

    @Test
    void should_set_draft_appeal_reference_when_appeal_started() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(featureToggler.getValue("share-case-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealReferenceNumberHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(APPEAL_REFERENCE_NUMBER, "DRAFT");
        verify(asylumCase, times(1)).write(LOCAL_AUTHORITY_POLICY, organisationPolicy);

        verifyNoInteractions(appealReferenceNumberGenerator);
    }

    @Test
    void should_set_next_appeal_reference_number_to_replace_draft_for_appeal_submitted() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        when(dateProvider.now()).thenReturn(LocalDate.of(2019, 10, 7));

        when(appealReferenceNumberGenerator.generate(123, AppealType.PA))
            .thenReturn("the-next-appeal-reference-number");

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER)).thenReturn(Optional.of("DRAFT"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealReferenceNumberHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(APPEAL_REFERENCE_NUMBER, "the-next-appeal-reference-number");
    }

    @Test
    void should_set_next_appeal_reference_number_to_replace_draft_for_appeal_submitted_and_paid() {

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);

        when(dateProvider.now()).thenReturn(LocalDate.of(2019, 10, 7));

        when(appealReferenceNumberGenerator.generate(123, AppealType.PA))
            .thenReturn("the-next-appeal-reference-number");

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER)).thenReturn(Optional.of("DRAFT"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealReferenceNumberHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(APPEAL_REFERENCE_NUMBER, "the-next-appeal-reference-number");
    }

    @Test
    void should_set_next_appeal_reference_number_if_not_present_for_submit_appeal() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        when(dateProvider.now()).thenReturn(LocalDate.of(2019, 10, 7));

        when(appealReferenceNumberGenerator.generate(123, AppealType.PA))
            .thenReturn("the-next-appeal-reference-number");

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealReferenceNumberHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(APPEAL_REFERENCE_NUMBER, "the-next-appeal-reference-number");
    }

    @Test
    void should_set_next_appeal_reference_number_if_not_present_for_pay_and_submit_appeal() {

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);

        when(dateProvider.now()).thenReturn(LocalDate.of(2019, 10, 7));

        when(appealReferenceNumberGenerator.generate(123, AppealType.PA))
            .thenReturn("the-next-appeal-reference-number");

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealReferenceNumberHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(APPEAL_REFERENCE_NUMBER, "the-next-appeal-reference-number");
    }

    @Test
    void should_do_nothing_if_non_draft_number_already_present_for_submit_appeal() {

        Optional<Object> appealReference = Optional.of("some-existing-reference-number");

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER)).thenReturn(appealReference);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        appealReferenceNumberHandler.handle(ABOUT_TO_SUBMIT, callback);

        verifyNoInteractions(appealReferenceNumberGenerator);
        verify(asylumCase, never()).write(any(), any());
    }

    @Test
    void should_do_nothing_if_non_draft_number_already_present_for_pay_and_submit_appeal() {

        Optional<Object> appealReference = Optional.of("some-existing-reference-number");

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER)).thenReturn(appealReference);
        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);

        appealReferenceNumberHandler.handle(ABOUT_TO_SUBMIT, callback);

        verifyNoInteractions(appealReferenceNumberGenerator);
        verify(asylumCase, never()).write(any(), any());
    }

    @Test
    void should_not_write_to_local_authority_policy_if_feature_not_enabled() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(featureToggler.getValue("share-case-feature", false)).thenReturn(false);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealReferenceNumberHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(APPEAL_REFERENCE_NUMBER, "DRAFT");
        verify(asylumCase, times(0)).write(LOCAL_AUTHORITY_POLICY, organisationPolicy);

        verifyNoInteractions(appealReferenceNumberGenerator);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = appealReferenceNumberHandler.canHandle(callbackStage, callback);

                if (Arrays.asList(
                    Event.START_APPEAL,
                    Event.SUBMIT_APPEAL,
                    Event.PAY_AND_SUBMIT_APPEAL)
                        .contains(callback.getEvent())
                    && callbackStage == ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> appealReferenceNumberHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> appealReferenceNumberHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealReferenceNumberHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealReferenceNumberHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealReferenceNumberHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
