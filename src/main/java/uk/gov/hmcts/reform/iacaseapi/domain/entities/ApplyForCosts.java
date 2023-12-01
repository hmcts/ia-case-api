package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@EqualsAndHashCode
@ToString
public class ApplyForCosts {
    private String appliedCostsType;
    private String argumentsAndEvidenceDetails;
    private List<IdValue<Document>> argumentsAndEvidenceDocuments;
    private List<IdValue<Document>> scheduleOfCostsDocuments;
    private YesOrNo applyForCostsHearingType;
    private String applyForCostsHearingTypeExplanation;
    private String applyForCostsDecision;
    private String applyForCostsApplicantType;
    private String applyForCostsCreationDate;
    private String respondentToCostsOrder;
    private String applyForCostsOotExplanation;
    private List<IdValue<Document>> ootUploadEvidenceDocuments;
    private YesOrNo isApplyForCostsOot;
    private String applyForCostsRespondentRole;
    private String responseToApplication;
    private YesOrNo responseHearingType;
    private String responseHearingTypeExplanation; //only if type of hearing is yes
    private List<IdValue<Document>> responseEvidence;
    private List<IdValue<Document>> applicantAdditionalEvidence;
    private List<IdValue<Document>> respondentAdditionalEvidence;


    public ApplyForCosts() {
        // noop -- for deserializer
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


    public String getAppliedCostsType() {
        requireNonNull(appliedCostsType);
        return appliedCostsType;
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

    public String getRespondentToCostsOrder() {
        requireNonNull(respondentToCostsOrder);
        return respondentToCostsOrder;
    }

    public String getApplyForCostsOotExplanation() {
        if (isApplyForCostsOot.equals(YesOrNo.YES)) {
            requireNonNull(applyForCostsOotExplanation);
        }
        return applyForCostsOotExplanation;
    }

    public String getApplyForCostsRespondentRole() {
        return applyForCostsRespondentRole;
    }

    public List<IdValue<Document>> getOotUploadEvidenceDocuments() {
        return ootUploadEvidenceDocuments;
    }

    public YesOrNo getIsApplyForCostsOot() {
        return isApplyForCostsOot;
    }

    public void setResponseToApplication(String responseToApplication) {
        this.responseToApplication = responseToApplication;
    }

    public String getResponseToApplication() {
        return responseToApplication;
    }

    public void setResponseHearingTypeExplanation(String responseHearingTypeExplanation) {
        this.responseHearingTypeExplanation = responseHearingTypeExplanation;
    }

    public void setEvidence(List<IdValue<Document>> evidence) {
        this.responseEvidence = evidence;
    }

    public String getResponseHearingTypeExplanation() {
        return responseHearingTypeExplanation;
    }

    public List<IdValue<Document>> getResponseEvidence() {
        return responseEvidence;
    }

    public YesOrNo getResponseHearingType() {
        return responseHearingType;
    }

    public void setResponseHearingType(YesOrNo responseHearingType) {
        this.responseHearingType = responseHearingType;
    }

    public List<IdValue<Document>> getApplicantAdditionalEvidence() {
        return applicantAdditionalEvidence;
    }

    public void setApplicantAdditionalEvidence(List<IdValue<Document>> applicantAdditionalEvidence) {
        this.applicantAdditionalEvidence = applicantAdditionalEvidence;
    }

    public List<IdValue<Document>> getRespondentAdditionalEvidence() {
        return respondentAdditionalEvidence;
    }

    public void setRespondentAdditionalEvidence(List<IdValue<Document>> respondentAdditionalEvidence) {
        this.respondentAdditionalEvidence = respondentAdditionalEvidence;
    }
}
