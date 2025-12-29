package uk.gov.hmcts.reform.bailcaseapi.infrastructure.service;

import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;

import java.util.Optional;

import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CASE_NAME_HMCTS_INTERNAL;

public class BailFieldCaseNameFixer implements DataFixer {

    private final BailCaseFieldDefinition hmctsCaseNameInternal;
    private final BailCaseFieldDefinition applicantGivenNames;
    private final BailCaseFieldDefinition applicantFamilyName;

    public BailFieldCaseNameFixer(
        BailCaseFieldDefinition hmctsCaseNameInternal,
        BailCaseFieldDefinition applicantGivenNames,
        BailCaseFieldDefinition applicantFamilyName) {
        this.hmctsCaseNameInternal = hmctsCaseNameInternal;
        this.applicantGivenNames = applicantGivenNames;
        this.applicantFamilyName = applicantFamilyName;
    }

    @Override
    public void fix(BailCase bailCase) {

        Optional<String> caseNameToBeUpdated = bailCase.read(hmctsCaseNameInternal);
        Optional<String> applicantGivenNamesToBeConcatenated = bailCase.read(applicantGivenNames);
        Optional<String> applicantFamilyNameToBeConcatenated = bailCase.read(applicantFamilyName);

        String expectedCaseName = null;

        if (applicantGivenNamesToBeConcatenated.isPresent() && applicantFamilyNameToBeConcatenated.isPresent()) {
            expectedCaseName = getCaseName(
                applicantGivenNamesToBeConcatenated.get(),
                applicantFamilyNameToBeConcatenated.get()
            );
        }

        if (expectedCaseName != null) {
            if (caseNameToBeUpdated.isEmpty()) {
                bailCase.write(CASE_NAME_HMCTS_INTERNAL, expectedCaseName);
            } else if (!caseNameToBeUpdated.get().equals(expectedCaseName)) {
                bailCase.write(CASE_NAME_HMCTS_INTERNAL, expectedCaseName);
            }
        }
    }

    public String getCaseName(String applicantGivenName, String applicantFamilyName) {
        String applicantCaseName = applicantGivenName + " " + applicantFamilyName;
        return applicantCaseName.replaceAll("\\s+", " ").trim();
    }
}
