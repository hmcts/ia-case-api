package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_ORGANISATION_REQUEST_FIELD;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CompanyNameProvider;

@Slf4j
@Component
public class LegalRepresentativeUpdateDetailsPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final CompanyNameProvider companyNameProvider;

    public LegalRepresentativeUpdateDetailsPreparer(CompanyNameProvider companyNameProvider) {
        this.companyNameProvider = companyNameProvider;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
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
            asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REP_NAME);
            asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REP_FAMILY_NAME);
            asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS);
            asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REP_REFERENCE_NUMBER);
        }

        String company = asylumCase.read(
            AsylumCaseFieldDefinition.LEGAL_REP_COMPANY, String.class)
            .orElse("");
        String name = asylumCase.read(
            AsylumCaseFieldDefinition.LEGAL_REP_NAME, String.class)
            .orElse("");
        String familyName = asylumCase.read(
                        AsylumCaseFieldDefinition.LEGAL_REP_FAMILY_NAME, String.class)
                .orElse("");
        String email = asylumCase.read(
            AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)
            .orElse("");
        String reference = asylumCase.read(
            AsylumCaseFieldDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class)
            .orElse("");

        asylumCase.write(AsylumCaseFieldDefinition.UPDATE_LEGAL_REP_COMPANY, company);
        asylumCase.write(AsylumCaseFieldDefinition.UPDATE_LEGAL_REP_NAME, name);
        asylumCase.write(AsylumCaseFieldDefinition.UPDATE_LEGAL_REP_FAMILY_NAME, familyName);
        asylumCase.write(AsylumCaseFieldDefinition.UPDATE_LEGAL_REP_EMAIL_ADDRESS, email);
        asylumCase.write(AsylumCaseFieldDefinition.UPDATE_LEGAL_REP_REFERENCE_NUMBER, reference);

        companyNameProvider.prepareCompanyName(callback);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
