package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@EqualsAndHashCode
@ToString
@Getter
@Setter
public class ApplyForCosts {
    //Applicant fields
    private String appliedCostsType;
    private String argumentsAndEvidenceDetails;
    private List<IdValue<Document>> argumentsAndEvidenceDocuments;
    private List<IdValue<Document>> scheduleOfCostsDocuments;
    private YesOrNo applyForCostsHearingType;
    private String applyForCostsHearingTypeExplanation;
    private String applyForCostsApplicantType;
    private String applyForCostsCreationDate;
    private String respondentToCostsOrder;
    private String applyForCostsOotExplanation;
    private List<IdValue<Document>> ootUploadEvidenceDocuments;
    private YesOrNo isApplyForCostsOot;

    //Respondent fields
    private String applyForCostsRespondentRole;
    private String responseToApplication;
    private YesOrNo responseHearingType;
    private String responseHearingTypeExplanation; //only if type of hearing is yes
    private List<IdValue<Document>> responseEvidence;

    //Both(applicant and respondent) additional evidence
    private List<IdValue<Document>> applicantAdditionalEvidence;
    private List<IdValue<Document>> respondentAdditionalEvidence;
    private String loggedUserRole;

    //Judge decision fields
    private String applyForCostsDecision;
    private String costsDecisionType;
    private String costsOralHearingDate; // only if costsDecisionType is "with an oral hearing"
    private List<IdValue<Document>> uploadCostsOrder;
    private String dateOfDecision;

    //Judge consideration fields
    private String tribunalConsideringReason;
    private List<IdValue<Document>> judgeEvidenceForCostsOrder;

    public ApplyForCosts() {
        // noop -- for deserializer
    }

    public ApplyForCosts(
        String applyForCostsDecision,
        String appliedCostsType,
        String applyForCostsApplicantType,
        String tribunalConsideringReason,
        List<IdValue<Document>> judgeEvidenceForCostsOrder,
        String applyForCostsCreationDate,
        String respondentToCostsOrder,
        String applyForCostsRespondentRole
    ) {
        requireNonNull(applyForCostsDecision);
        requireNonNull(appliedCostsType);
        requireNonNull(applyForCostsApplicantType);
        requireNonNull(tribunalConsideringReason);
        requireNonNull(applyForCostsCreationDate);
        requireNonNull(respondentToCostsOrder);
        requireNonNull(applyForCostsRespondentRole);

        this.applyForCostsDecision = applyForCostsDecision;
        this.appliedCostsType = appliedCostsType;
        this.applyForCostsApplicantType = applyForCostsApplicantType;
        this.tribunalConsideringReason = tribunalConsideringReason;
        this.judgeEvidenceForCostsOrder = judgeEvidenceForCostsOrder;
        this.applyForCostsCreationDate = applyForCostsCreationDate;
        this.respondentToCostsOrder = respondentToCostsOrder;
        this.applyForCostsRespondentRole = applyForCostsRespondentRole;
    }

    public ApplyForCosts(
            String appliedCostsType,
            String argumentsAndEvidenceDetails,
            List<IdValue<Document>> argumentsAndEvidenceDocuments,
            List<IdValue<Document>> scheduleOfCostsDocuments,
            YesOrNo applyForCostsHearingType,
            String applyForCostsHearingTypeExplanation,
            String applyForCostsDecision,
            String applyForCostsApplicantType,
            String applyForCostsCreationDate,
            String respondentToCostsOrder,
            String applyForCostsOotExplanation,
            List<IdValue<Document>> ootUploadEvidenceDocuments,
            YesOrNo isApplyForCostsOot,
            String applyForCostsRespondentRole
    ) {
        requireNonNull(appliedCostsType);
        requireNonNull(argumentsAndEvidenceDocuments);
        requireNonNull(applyForCostsHearingType);
        requireNonNull(applyForCostsDecision);
        requireNonNull(applyForCostsApplicantType);
        requireNonNull(applyForCostsCreationDate);

        if (applyForCostsHearingType.equals(YesOrNo.YES)) {
            requireNonNull(applyForCostsHearingTypeExplanation);
        }
        requireNonNull(respondentToCostsOrder);

        if (isApplyForCostsOot.equals(YesOrNo.YES)) {
            requireNonNull(applyForCostsOotExplanation);
        }

        this.appliedCostsType = appliedCostsType;
        this.argumentsAndEvidenceDetails = argumentsAndEvidenceDetails;
        this.argumentsAndEvidenceDocuments = argumentsAndEvidenceDocuments;
        this.scheduleOfCostsDocuments = scheduleOfCostsDocuments;
        this.applyForCostsHearingType = applyForCostsHearingType;
        this.applyForCostsHearingTypeExplanation = applyForCostsHearingTypeExplanation;
        this.applyForCostsDecision = applyForCostsDecision;
        this.applyForCostsApplicantType = applyForCostsApplicantType;
        this.applyForCostsCreationDate = applyForCostsCreationDate;
        this.respondentToCostsOrder = respondentToCostsOrder;
        this.applyForCostsOotExplanation = applyForCostsOotExplanation;
        this.ootUploadEvidenceDocuments = ootUploadEvidenceDocuments;
        this.isApplyForCostsOot = isApplyForCostsOot;
        this.applyForCostsRespondentRole = applyForCostsRespondentRole;
    }
}
