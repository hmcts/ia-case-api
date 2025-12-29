package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_NAME_HMCTS_INTERNAL;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;

public class AsylumFieldCaseNameFixer implements DataFixer<AsylumCase> {

    private final AsylumCaseFieldDefinition hmctsCaseNameInternal;
    private final AsylumCaseFieldDefinition appellantGivenNames;
    private final AsylumCaseFieldDefinition appellantFamilyName;

    private final FeatureToggler featureToggler;

    public AsylumFieldCaseNameFixer(
        AsylumCaseFieldDefinition hmctsCaseNameInternal,
        AsylumCaseFieldDefinition appellantGivenNames,
        AsylumCaseFieldDefinition appellantFamilyName,
        FeatureToggler featureToggler) {
        this.hmctsCaseNameInternal = hmctsCaseNameInternal;
        this.appellantGivenNames = appellantGivenNames;
        this.appellantFamilyName = appellantFamilyName;
        this.featureToggler = featureToggler;
    }

    @Override
    public void fix(AsylumCase asylumCase) {

        Optional<Object> caseNameToBeTransitioned = asylumCase.read(hmctsCaseNameInternal);
        Optional<Object> appellantGivenNamesToBeConcatenated = asylumCase.read(appellantGivenNames);
        Optional<Object> appellantFamilyNameToBeConcatenated = asylumCase.read(appellantFamilyName);
        boolean migrateCaseName = featureToggler.getValue("wa-R3-feature", false);

        String expectedCaseName = null;

        if (appellantGivenNamesToBeConcatenated.isPresent() && appellantFamilyNameToBeConcatenated.isPresent()) {
            expectedCaseName = getCaseName(appellantGivenNamesToBeConcatenated.get().toString(), appellantFamilyNameToBeConcatenated.get().toString());
        }

        if (expectedCaseName != null && ((caseNameToBeTransitioned.isPresent() && !caseNameToBeTransitioned.get().toString().equals(expectedCaseName)) || caseNameToBeTransitioned.isEmpty())) {
            asylumCase.write(hmctsCaseNameInternal, expectedCaseName);
            if (migrateCaseName) {
                asylumCase.write(CASE_NAME_HMCTS_INTERNAL, expectedCaseName);
            }
        }

        if (migrateCaseName && asylumCase.read(CASE_NAME_HMCTS_INTERNAL).isEmpty()) {
            asylumCase.write(CASE_NAME_HMCTS_INTERNAL, expectedCaseName);
        }
    }

    public String getCaseName(String appealReferenceNumberToBeConcatenated, String appellantFamilyNameToBeConcatenated) {

        String appellantNameForDisplay = appealReferenceNumberToBeConcatenated + " " + appellantFamilyNameToBeConcatenated;

        return appellantNameForDisplay.replaceAll("\\s+", " ").trim();
    }
}
