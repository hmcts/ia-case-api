package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Organisation;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.OrganisationPolicy;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ref.OrganisationEntityResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.ProfessionalOrganisationRetriever;

@Slf4j
@Service
public class LegalRepOrganisationFormatter implements PreSubmitCallbackHandler<BailCase> {

    private final ProfessionalOrganisationRetriever professionalOrganisationRetriever;

    public LegalRepOrganisationFormatter(
        ProfessionalOrganisationRetriever professionalOrganisationRetriever
    ) {
        this.professionalOrganisationRetriever = professionalOrganisationRetriever;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.START_APPLICATION;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage,
                                                      Callback<BailCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final OrganisationEntityResponse organisationEntityResponse =
            professionalOrganisationRetriever.retrieve();

        if (organisationEntityResponse == null) {
            log.warn("Data fetched from Professional Ref data is empty, case ID: {}",
                     callback.getCaseDetails().getId());
        }

        if (organisationEntityResponse != null
            && StringUtils.isNotBlank(organisationEntityResponse.getOrganisationIdentifier())) {

            log.info("PRD endpoint called for caseId [{}] orgId[{}]",
                     callback.getCaseDetails().getId(), organisationEntityResponse.getOrganisationIdentifier());

            setupCaseCreation(callback, organisationEntityResponse.getOrganisationIdentifier());
        }

        return new PreSubmitCallbackResponse<>(callback.getCaseDetails().getCaseData());
    }

    private void setupCaseCreation(Callback<BailCase> callback, String organisationIdentifier) {

        BailCase bailCase = callback.getCaseDetails().getCaseData();

        final OrganisationPolicy organisationPolicy =
            OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                                  .organisationID(organisationIdentifier)
                                  .build()
                )
                .orgPolicyCaseAssignedRole("[LEGALREPRESENTATIVE]")
                .build();

        bailCase.write(BailCaseFieldDefinition.LOCAL_AUTHORITY_POLICY, organisationPolicy);
    }
}

