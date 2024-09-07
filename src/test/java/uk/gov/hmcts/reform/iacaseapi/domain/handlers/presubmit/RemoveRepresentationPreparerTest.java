package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDateTime;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.OrganisationPolicy;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RemoveRepresentationPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private OrganisationPolicy organisationPolicy;

    private ChangeOrganisationRequest changeOrganisationRequest;
    private RemoveRepresentationPreparer removeRepresentationPreparer;

    @BeforeEach
    public void setUp() {

        removeRepresentationPreparer =
            new RemoveRepresentationPreparer();

        Value caseRole =
            new Value("[LEGALREPRESENTATIVE]", "Legal Representative");

        changeOrganisationRequest = new ChangeOrganisationRequest(
            new DynamicList(caseRole, newArrayList(caseRole)),
            LocalDateTime.now().toString(),
            "1"
        );
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REMOVE_REPRESENTATION", "REMOVE_LEGAL_REPRESENTATIVE"
    })
    void should_write_to_remove_representation_requested_flag_field(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.LOCAL_AUTHORITY_POLICY)).thenReturn(Optional.of(organisationPolicy));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            removeRepresentationPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        assertThat(callbackResponse.getErrors()).isEmpty();

        verify(asylumCase, times(1)).read(LOCAL_AUTHORITY_POLICY);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> removeRepresentationPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(
            () -> removeRepresentationPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REMOVE_REPRESENTATION", "REMOVE_LEGAL_REPRESENTATIVE"
    })
    void should_respond_with_error_for_legal_rep_when_organisation_policy_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REMOVE_REPRESENTATION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(LOCAL_AUTHORITY_POLICY)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response =
            removeRepresentationPreparer.handle(
                PreSubmitCallbackStage.ABOUT_TO_START,
                callback
            );

        assertThat(response.getData()).isInstanceOf(AsylumCase.class);
        assertThat(response.getErrors()).contains("You cannot use this feature because the legal representative does not have a MyHMCTS account or the appeal was created before 10 February 2021.");
        assertThat(response.getErrors()).contains("If you are a legal representative, you must contact all parties confirming you no longer represent this client.");

        verify(asylumCase, times(1)).read(LOCAL_AUTHORITY_POLICY);
        verify(asylumCase, times(0)).write(CHANGE_ORGANISATION_REQUEST_FIELD, changeOrganisationRequest);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REMOVE_REPRESENTATION", "REMOVE_LEGAL_REPRESENTATIVE"
    })
    void should_return_error_when_remove_representation_requested_and_previous_request_still_in_process(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.LOCAL_AUTHORITY_POLICY)).thenReturn(Optional.of(organisationPolicy));
        when(asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class)).thenReturn(Optional.of(changeOrganisationRequest));

        PreSubmitCallbackResponse<AsylumCase> response =
            removeRepresentationPreparer.handle(
                PreSubmitCallbackStage.ABOUT_TO_START,
                callback
            );

        assertThat(response.getData()).isInstanceOf(AsylumCase.class);
        assertThat(response.getErrors()).contains("A request to remove representation is still being processed.\nPlease try again in a few minutes. If the issue persists, please contact the administrator.");

        verify(asylumCase, times(1)).read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class);
        verify(asylumCase, times(0)).write(CHANGE_ORGANISATION_REQUEST_FIELD, changeOrganisationRequest);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = removeRepresentationPreparer.canHandle(callbackStage, callback);

                if ((event == Event.REMOVE_REPRESENTATION || event == Event.REMOVE_LEGAL_REPRESENTATIVE)
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> removeRepresentationPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> removeRepresentationPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> removeRepresentationPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> removeRepresentationPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
