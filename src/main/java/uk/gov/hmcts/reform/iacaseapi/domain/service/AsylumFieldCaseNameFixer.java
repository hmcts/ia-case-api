package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;

public class AsylumFieldCaseNameFixer implements DataFixer {

    private final AsylumCaseFieldDefinition hmctsCaseNameInternal;
    private final AsylumCaseFieldDefinition appellantGivenNames;
    private final AsylumCaseFieldDefinition appellantFamilyName;

    public AsylumFieldCaseNameFixer(
        AsylumCaseFieldDefinition hmctsCaseNameInternal,
        AsylumCaseFieldDefinition appellantGivenNames,
        AsylumCaseFieldDefinition appellantFamilyName
    ) {
        this.hmctsCaseNameInternal = hmctsCaseNameInternal;
        this.appellantGivenNames = appellantGivenNames;
        this.appellantFamilyName = appellantFamilyName;
    }

    @Override
    public void fix(AsylumCase asylumCase) {

        Optional<Object> caseNameToBeTransitioned = asylumCase.read(hmctsCaseNameInternal);
        Optional<Object> appellantGivenNamesToBeConcatenated = asylumCase.read(appellantGivenNames);
        Optional<Object> appellantFamilyNameToBeConcatenated = asylumCase.read(appellantFamilyName);

        String expectedCaseName = null;

        if (appellantGivenNamesToBeConcatenated.isPresent() && appellantFamilyNameToBeConcatenated.isPresent()) {
            expectedCaseName = getCaseName(appellantGivenNamesToBeConcatenated.get().toString(), appellantFamilyNameToBeConcatenated.get().toString());
        }

        if (expectedCaseName != null && ((caseNameToBeTransitioned.isPresent() && !caseNameToBeTransitioned.get().toString().equals(expectedCaseName)) || caseNameToBeTransitioned.isEmpty())) {
            asylumCase.write(hmctsCaseNameInternal, expectedCaseName);
        }
    }

    public String getCaseName(String appealReferenceNumberToBeConcatenated, String appellantFamilyNameToBeConcatenated) {

        String appellantNameForDisplay = appealReferenceNumberToBeConcatenated + " " + appellantFamilyNameToBeConcatenated;

        return appellantNameForDisplay.replaceAll("\\s+", " ").trim();
    }
}
