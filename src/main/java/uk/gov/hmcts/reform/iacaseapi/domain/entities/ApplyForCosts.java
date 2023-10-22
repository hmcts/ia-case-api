package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@EqualsAndHashCode
@ToString
public class ApplyForCosts {
    private TypesOfAppliedCosts typesOfAppliedCosts;
    private String argumentsAndEvidenceDetails;
    private List<IdValue<Document>> argumentsAndEvidenceDocuments;
    private List<IdValue<Document>> scheduleOfCostsDocuments;
    private YesOrNo applyForCostsHearingType;
    private String applyForCostsHearingTypeExplanation;
    private String applyForCostsDecision;
    private String applyForCostsApplicantType;
    private String applyForCostsCreationDate;

    public ApplyForCosts() {
        // noop -- for deserializer
    }

    public ApplyForCosts(
            TypesOfAppliedCosts typesOfAppliedCosts,
            String argumentsAndEvidenceDetails,
            List<IdValue<Document>> argumentsAndEvidenceDocuments,
            List<IdValue<Document>> scheduleOfCostsDocuments,
            YesOrNo applyForCostsHearingType,
            String applyForCostsHearingTypeExplanation,
            String applyForCostsDecision,
            String applyForCostsApplicantType,
            String applyForCostsCreationDate
    ) {
        requireNonNull(typesOfAppliedCosts);
        requireNonNull(argumentsAndEvidenceDocuments);
        requireNonNull(applyForCostsHearingType);
        requireNonNull(applyForCostsDecision);
        requireNonNull(applyForCostsApplicantType);
        requireNonNull(applyForCostsCreationDate);
        if (applyForCostsHearingType.equals(YesOrNo.YES)) {
            requireNonNull(applyForCostsHearingTypeExplanation);
        }

        this.typesOfAppliedCosts = typesOfAppliedCosts;
        this.argumentsAndEvidenceDetails = argumentsAndEvidenceDetails;
        this.argumentsAndEvidenceDocuments = argumentsAndEvidenceDocuments;
        this.scheduleOfCostsDocuments = scheduleOfCostsDocuments;
        this.applyForCostsHearingType = applyForCostsHearingType;
        this.applyForCostsHearingTypeExplanation = applyForCostsHearingTypeExplanation;
        this.applyForCostsDecision = applyForCostsDecision;
        this.applyForCostsApplicantType = applyForCostsApplicantType;
        this.applyForCostsCreationDate = applyForCostsCreationDate;
    }


    public TypesOfAppliedCosts getTypesOfAppliedCosts() {
        requireNonNull(typesOfAppliedCosts);
        return typesOfAppliedCosts;
    }

    public String getArgumentsAndEvidenceDetails() {
        return argumentsAndEvidenceDetails;
    }

    public List<IdValue<Document>> getArgumentsAndEvidenceDocuments() {
        requireNonNull(argumentsAndEvidenceDocuments);
        return argumentsAndEvidenceDocuments;
    }

    public List<IdValue<Document>> getScheduleOfCostsDocuments() {
        return scheduleOfCostsDocuments;
    }

    public YesOrNo getApplyForCostsHearingType() {
        requireNonNull(applyForCostsHearingType);
        return applyForCostsHearingType;
    }

    public String getApplyForCostsHearingTypeExplanation() {
        if (getApplyForCostsHearingType().equals(YesOrNo.YES)) {
            requireNonNull(applyForCostsHearingTypeExplanation);
        }
        return applyForCostsHearingTypeExplanation;
    }


    public String getApplyForCostsDecision() {
        requireNonNull(applyForCostsDecision);
        return applyForCostsDecision;
    }

    public String getApplyForCostsApplicantType() {
        requireNonNull(applyForCostsApplicantType);
        return applyForCostsApplicantType;
    }

    public String getApplyForCostsCreationDate() {
        requireNonNull(applyForCostsCreationDate);
        return applyForCostsCreationDate;
    }
}
