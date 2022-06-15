package uk.gov.hmcts.reform.bailcaseapi.domain.service;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ref.OrganisationEntityResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.ProfessionalOrganisationRetriever;

import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_COMPANY;

@Slf4j
@Service
public class CompanyNameProvider {

    private final ProfessionalOrganisationRetriever professionalOrganisationRetriever;

    public CompanyNameProvider(ProfessionalOrganisationRetriever professionalOrganisationRetriever) {
        this.professionalOrganisationRetriever = professionalOrganisationRetriever;
    }

    public void prepareCompanyName(Callback<BailCase> callback) {

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

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

            String organisationName = organisationEntityResponse.getName() == null
                ? "" : organisationEntityResponse.getName();

            if (callback.getEvent() == Event.START_APPLICATION || callback.getEvent() == Event.MAKE_NEW_APPLICATION) {
                bailCase.write(LEGAL_REP_COMPANY, organisationName);
            }
        }
    }
}
