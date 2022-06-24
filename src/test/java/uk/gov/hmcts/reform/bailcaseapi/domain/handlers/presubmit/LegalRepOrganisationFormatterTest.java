package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LOCAL_AUTHORITY_POLICY;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Organisation;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.OrganisationPolicy;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ref.OrganisationEntityResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.ProfessionalOrganisationRetriever;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class LegalRepOrganisationFormatterTest {

    private LegalRepOrganisationFormatter legalRepOrganisationFormatter;
    private String companyName = "LBC";
    private long ccdCaseId = 12345L;
    private String organisationIdentifier = "ZE2KIWO";
    private OrganisationPolicy organisationPolicy;

    @Mock ProfessionalOrganisationRetriever professionalOrganisationRetriever;
    @Mock OrganisationEntityResponse organisationEntityResponse;

    @Mock private Callback<BailCase> callback;
    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;

    @BeforeEach
    public void setUp() throws Exception {

        legalRepOrganisationFormatter = new LegalRepOrganisationFormatter(
            professionalOrganisationRetriever
        );

        organisationPolicy =
            OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                                  .organisationID(organisationIdentifier)
                                  .build()
                )
                .orgPolicyCaseAssignedRole("[LEGALREPRESENTATIVE]")
                .build();
    }

    @Test
    void should_respond_with_bail_case_with_results() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(callback.getCaseDetails().getId()).thenReturn(ccdCaseId);

        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);

        PreSubmitCallbackResponse<BailCase> response =
            legalRepOrganisationFormatter.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            );

        verify(bailCase, times(1)).write(LOCAL_AUTHORITY_POLICY, organisationPolicy);
    }

    @Test
    void should_not_write_to_local_authority_policy_if_organisation_entity_response_is_null() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(professionalOrganisationRetriever.retrieve()).thenReturn(null);

        PreSubmitCallbackResponse<BailCase> response =
            legalRepOrganisationFormatter.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            );

        assertEquals(bailCase, response.getData());

        verify(bailCase, times(0)).write(LOCAL_AUTHORITY_POLICY, organisationPolicy);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = legalRepOrganisationFormatter.canHandle(callbackStage, callback);

                if (event == Event.START_APPLICATION
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

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

        assertThatThrownBy(() -> legalRepOrganisationFormatter.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> legalRepOrganisationFormatter.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
