package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;

public class AsylumFieldCaseCategoryFixer implements DataFixer<AsylumCase> {

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
                mapToWaDescriptions(asylumCase);
            }
        }

        if (hmctsCaseCategoryToBeTransitioned.isEmpty()) {
            if (appealTypeToBeCopied.isPresent()) {
                mapToWaDescriptions(asylumCase);
            }
        }
    }

    public void mapToWaDescriptions(AsylumCase asylumCase) {

        AppealType caseAppealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new IllegalStateException("Appeal type is not present"));

        switch (caseAppealType) {

            case EU:
                asylumCase.write(hmctsCaseCategory, ("EU Settlement Scheme"));
                break;

            case RP:
                asylumCase.write(hmctsCaseCategory, ("Revocation"));
                break;

            case PA:
                asylumCase.write(hmctsCaseCategory, ("Protection"));
                break;

            case EA:
                asylumCase.write(hmctsCaseCategory, ("EEA"));
                break;

            case HU:
                asylumCase.write(hmctsCaseCategory, ("Human rights"));
                break;

            case DC:
                asylumCase.write(hmctsCaseCategory, ("DoC"));
                break;

            default:
                break;
        }
    }
}
