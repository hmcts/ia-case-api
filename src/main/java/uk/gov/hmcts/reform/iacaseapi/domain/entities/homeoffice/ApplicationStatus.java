package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

public class ApplicationStatus {

    private CodeWithDescription applicationType;
    private CodeWithDescription claimReasonType;
    private DecisionCommunication decisionCommunication;
    private String decisionDate;
    private CodeWithDescription decisionType;
    private String documentReference;
    private CodeWithDescription roleSubType;
    private CodeWithDescription roleType;
    @JsonProperty("metadata")
    private List<HomeOfficeMetadata> homeOfficeMetadata;
    private List<RejectionReason> rejectionReasons;
    private List<IdValue<HomeOfficeMetadata>> ccdHomeOfficeMetadata;
    private List<IdValue<RejectionReason>> ccdRejectionReasons;

    private ApplicationStatus() {

    }

    public ApplicationStatus(
        CodeWithDescription applicationType,
        CodeWithDescription claimReasonType,
        DecisionCommunication decisionCommunication,
        String decisionDate,
        CodeWithDescription decisionType,
        String documentReference,
        CodeWithDescription roleSubType,
        CodeWithDescription roleType,
        List<HomeOfficeMetadata> homeOfficeMetadata,
        List<RejectionReason> rejectionReasons) {
        this.applicationType = applicationType;
        this.claimReasonType = claimReasonType;
        this.decisionCommunication = decisionCommunication;
        this.decisionDate = decisionDate;
        this.decisionType = decisionType;
        this.documentReference = documentReference;
        this.roleSubType = roleSubType;
        this.roleType = roleType;
        this.homeOfficeMetadata = homeOfficeMetadata;
        this.rejectionReasons = rejectionReasons;
    }

    public CodeWithDescription getDecisionType() {
        return decisionType;
    }

    public String getDecisionDate() {
        return decisionDate;
    }

    public CodeWithDescription getApplicationType() {
        return applicationType;
    }

    public CodeWithDescription getClaimReasonType() {
        return claimReasonType;
    }

    public DecisionCommunication getDecisionCommunication() {
        return decisionCommunication;
    }

    public String getDocumentReference() {
        return documentReference;
    }

    public CodeWithDescription getRoleSubType() {
        return roleSubType;
    }

    public CodeWithDescription getRoleType() {
        return roleType;
    }

    public List<HomeOfficeMetadata> getHomeOfficeMetadata() {
        return homeOfficeMetadata;
    }

    public List<RejectionReason> getRejectionReasons() {
        return rejectionReasons;
    }

    public List<IdValue<HomeOfficeMetadata>> getCcdHomeOfficeMetadata() {
        return ccdHomeOfficeMetadata;
    }

    public List<IdValue<RejectionReason>> getCcdRejectionReasons() {
        return ccdRejectionReasons;
    }

    public void modifyListDataForCcd() {
        this.ccdHomeOfficeMetadata = new ArrayList<>();
        if (homeOfficeMetadata != null && !homeOfficeMetadata.isEmpty()) {
            AtomicInteger index = new AtomicInteger(0);
            homeOfficeMetadata.stream().forEach(
                metadata -> this.ccdHomeOfficeMetadata.add(
                    new IdValue<>(String.valueOf(index.getAndIncrement()), metadata))
            );
        }
        this.ccdRejectionReasons = new ArrayList<>();
        if (rejectionReasons != null && !rejectionReasons.isEmpty()) {
            AtomicInteger index = new AtomicInteger(0);
            rejectionReasons.stream().forEach(
                reason -> this.ccdRejectionReasons.add(new IdValue<>(String.valueOf(index.getAndIncrement()), reason))
            );
        }
        this.homeOfficeMetadata = null;
        this.rejectionReasons = null;
    }
}

