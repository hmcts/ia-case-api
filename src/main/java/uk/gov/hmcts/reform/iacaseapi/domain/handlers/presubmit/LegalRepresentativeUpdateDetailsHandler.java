package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PREVIOUS_REPRESENTATIONS;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PreviousRepresentation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ref.OrganisationEntityResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PreviousRepresentationAppender;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ProfessionalOrganisationRetriever;

@Slf4j
@Component
public class LegalRepresentativeUpdateDetailsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final ProfessionalOrganisationRetriever professionalOrganisationRetriever;
    private final PreviousRepresentationAppender previousRepresentationAppender;

    public LegalRepresentativeUpdateDetailsHandler(
        ProfessionalOrganisationRetriever professionalOrganisationRetriever,
        PreviousRepresentationAppender previousRepresentationAppender
    ) {
        this.professionalOrganisationRetriever = professionalOrganisationRetriever;
        this.previousRepresentationAppender = previousRepresentationAppender;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.UPDATE_LEGAL_REPRESENTATIVES_DETAILS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final Optional<ChangeOrganisationRequest> changeOrganisationRequest = asylumCase
            .read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class);

        if (changeOrganisationRequest.isPresent()) {
            writeToPreviousRepresentations(callback, retrieveNewCompanyName(callback, professionalOrganisationRetriever));
        }

        String name = asylumCase.read(
            AsylumCaseFieldDefinition.UPDATE_LEGAL_REP_NAME, String.class)
            .orElse("");
        String email = asylumCase.read(
            AsylumCaseFieldDefinition.UPDATE_LEGAL_REP_EMAIL_ADDRESS, String.class)
            .orElse("");
        String reference = asylumCase.read(
            AsylumCaseFieldDefinition.UPDATE_LEGAL_REP_REFERENCE_NUMBER, String.class)
            .orElse("");

        asylumCase.clear(AsylumCaseFieldDefinition.UPDATE_LEGAL_REP_COMPANY);
        asylumCase.clear(AsylumCaseFieldDefinition.UPDATE_LEGAL_REP_NAME);
        asylumCase.clear(AsylumCaseFieldDefinition.UPDATE_LEGAL_REP_EMAIL_ADDRESS);
        asylumCase.clear(AsylumCaseFieldDefinition.UPDATE_LEGAL_REP_REFERENCE_NUMBER);

        asylumCase.write(AsylumCaseFieldDefinition.LEGAL_REP_NAME, name);
        asylumCase.write(AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, email);
        asylumCase.write(AsylumCaseFieldDefinition.LEGAL_REP_REFERENCE_NUMBER, reference);

        // remove the field which is used to suppress notifications after appeal is transferred to another Legal Rep firm
        asylumCase.clear(AsylumCaseFieldDefinition.CHANGE_ORGANISATION_REQUEST_FIELD);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    void writeToPreviousRepresentations(Callback<AsylumCase> callback, String updatedOrganisationName) {

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        asylumCase.write(UPDATE_LEGAL_REP_COMPANY, updatedOrganisationName);

        Optional<List<IdValue<PreviousRepresentation>>> maybePreviousRepresentations =
            asylumCase.read(PREVIOUS_REPRESENTATIONS);

        final List<IdValue<PreviousRepresentation>> existingPreviousRepresentations =
            maybePreviousRepresentations.orElse(Collections.emptyList());

        final String legalRepCompany = asylumCase.read(LEGAL_REP_COMPANY, String.class)
            .orElse("");

        final String legalRepReferenceNumber = asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)
            .orElse("");

        PreviousRepresentation previousRepresentation = new PreviousRepresentation(
            legalRepCompany,
            legalRepReferenceNumber
        );

        List<IdValue<PreviousRepresentation>> allPreviousRepresentations =
            previousRepresentationAppender.append(
                existingPreviousRepresentations,
                previousRepresentation
            );

        asylumCase.write(PREVIOUS_REPRESENTATIONS, allPreviousRepresentations);
        asylumCase.write(LEGAL_REP_COMPANY, updatedOrganisationName);
    }

    String retrieveNewCompanyName(Callback<AsylumCase> callback, ProfessionalOrganisationRetriever professionalOrganisationRetriever) {

        String organisationName = "";

        final OrganisationEntityResponse organisationEntityResponse =
            professionalOrganisationRetriever.retrieve();

        if (organisationEntityResponse == null) {
            log.warn("Data fetched from Professional Ref data is empty, case ID: {}", callback.getCaseDetails().getId());
        }

        if (organisationEntityResponse != null
            && StringUtils.isNotBlank(organisationEntityResponse.getOrganisationIdentifier())) {

            log.info("PRD endpoint called for caseId [{}] orgId[{}]",
                callback.getCaseDetails().getId(), organisationEntityResponse.getOrganisationIdentifier());

            organisationName = organisationEntityResponse.getName() == null
                ? "" : organisationEntityResponse.getName();
        }

        return organisationName;
    }
}
