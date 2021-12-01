package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;

public class AsylumFieldCaseCategoryFixer implements DataFixer {

    private final AsylumCaseFieldDefinition hmctsCaseCategory;
    private final AsylumCaseFieldDefinition appealType;

    public AsylumFieldCaseCategoryFixer(
        AsylumCaseFieldDefinition hmctsCaseCategory,
        AsylumCaseFieldDefinition appealType
    ) {
        this.hmctsCaseCategory = hmctsCaseCategory;
        this.appealType = appealType;
    }

    @Override
    public void fix(AsylumCase asylumCase) {

        Optional<Object> hmctsCaseCategoryToBeTransitioned = asylumCase.read(hmctsCaseCategory);
        Optional<Object> appealTypeToBeCopied = asylumCase.read(appealType);

        if (hmctsCaseCategoryToBeTransitioned.isPresent() && appealTypeToBeCopied.isPresent()) {
            if (hmctsCaseCategoryToBeTransitioned.get() != appealTypeToBeCopied.get()) {
                asylumCase.write(hmctsCaseCategory, ((AppealType) appealTypeToBeCopied.get()).getDescription());
            }
        }

        if (hmctsCaseCategoryToBeTransitioned.isEmpty()) {
            if (appealTypeToBeCopied.isPresent()) {
                asylumCase.write(hmctsCaseCategory, ((AppealType) appealTypeToBeCopied.get()).getDescription());
            }
        }
    }
}
