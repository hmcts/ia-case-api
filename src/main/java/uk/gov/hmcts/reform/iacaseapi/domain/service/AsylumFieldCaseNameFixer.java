package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;

public class AsylumFieldCaseNameFixer implements DataFixer {

    private final AsylumCaseFieldDefinition caseName;
    private final AsylumCaseFieldDefinition appealReferenceNumber;
    private final AsylumCaseFieldDefinition appellantFamilyName;

    public AsylumFieldCaseNameFixer(
        AsylumCaseFieldDefinition caseName,
        AsylumCaseFieldDefinition appealReferenceNumber,
        AsylumCaseFieldDefinition appellantFamilyName
    ) {
        this.caseName = caseName;
        this.appealReferenceNumber = appealReferenceNumber;
        this.appellantFamilyName = appellantFamilyName;
    }

    @Override
    public void fix(AsylumCase asylumCase) {

        Optional<Object> caseNameToBeTransitioned = asylumCase.read(caseName);
        Optional<Object> appealReferenceNumberToBeConcatenated = asylumCase.read(appealReferenceNumber);
        Optional<Object> appellantFamilyNameToBeConcatenated = asylumCase.read(appellantFamilyName);

        if (caseNameToBeTransitioned.isEmpty() && appealReferenceNumberToBeConcatenated.isPresent() && appellantFamilyNameToBeConcatenated.isPresent()) {

            asylumCase.write(caseName, getCaseName(appealReferenceNumberToBeConcatenated.get().toString(), appellantFamilyNameToBeConcatenated.get().toString()));
        }

        if (caseNameToBeTransitioned.toString().contains("DRAFT-") && appealReferenceNumberToBeConcatenated.isPresent() && appellantFamilyNameToBeConcatenated.isPresent()) {

            asylumCase.write(caseName, getCaseName(appealReferenceNumberToBeConcatenated.get().toString(), appellantFamilyNameToBeConcatenated.get().toString()));
        }
    }

    public String getCaseName(String appealReferenceNumberToBeConcatenated, String appellantFamilyNameToBeConcatenated) {

        return appealReferenceNumberToBeConcatenated + "-" + appellantFamilyNameToBeConcatenated;
    }
}
