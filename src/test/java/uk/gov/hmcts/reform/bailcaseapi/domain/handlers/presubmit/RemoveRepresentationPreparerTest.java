package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CHANGE_ORGANISATION_REQUEST_FIELD;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LOCAL_AUTHORITY_POLICY;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.OrganisationPolicy;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.ChangeOrganisationRequest;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RemoveRepresentationPreparerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
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

    // There's only REMOVE_BAIL_LEGAL_REPRESENTATIVE for now, but more events can be added to the list
    // (e.g. STOP_LEGAL_REPRESENTING)
    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REMOVE_BAIL_LEGAL_REPRESENTATIVE", "STOP_LEGAL_REPRESENTING"
    })
    void should_write_to_remove_representation_requested_flag_field(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class))
            .thenReturn(Optional.of(organisationPolicy));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            removeRepresentationPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());
        assertThat(callbackResponse.getErrors()).isEmpty();

        verify(bailCase, times(1)).read(LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> removeRepresentationPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_BAIL_DIRECTION); // purposefully wrong event
        assertThatThrownBy(
            () -> removeRepresentationPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    // There's only REMOVE_BAIL_LEGAL_REPRESENTATIVE for now, but more events can be added to the list
    // (e.g. STOP_LEGAL_REPRESENTING)
    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REMOVE_BAIL_LEGAL_REPRESENTATIVE", "STOP_LEGAL_REPRESENTING"
    })
    void should_respond_with_error_for_legal_rep_when_organisation_policy_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REMOVE_BAIL_LEGAL_REPRESENTATIVE);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<BailCase> response =
            removeRepresentationPreparer.handle(
                PreSubmitCallbackStage.ABOUT_TO_START,
                callback
            );

        assertThat(response.getData()).isInstanceOf(BailCase.class);
        assertThat(response.getErrors()).contains("You cannot use this feature because the legal representative "
                                                  + "does not have a MyHMCTS account.");
        assertThat(response.getErrors()).contains("If you are a legal representative, you must contact all parties "
                                                  + "confirming you no longer represent this client.");

        verify(bailCase, times(1)).read(LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class);
        verify(bailCase, never()).write(CHANGE_ORGANISATION_REQUEST_FIELD, changeOrganisationRequest);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = removeRepresentationPreparer.canHandle(callbackStage, callback);

                if ((event == Event.REMOVE_BAIL_LEGAL_REPRESENTATIVE || event == Event.STOP_LEGAL_REPRESENTING)
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
