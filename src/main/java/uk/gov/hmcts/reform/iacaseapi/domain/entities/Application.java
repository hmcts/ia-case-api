package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

import java.util.List;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

public class Application {

    private List<IdValue<Document>> applicationDocuments;
    private String applicationSupplier;
    private String applicationType;
    private String applicationReason;
    private String applicationDate;
    private String applicationDecision;
    private String applicationDecisionReason;
    private String applicationDateOfDecision;
    private String applicationStatus;

    private Application() {
    }

    public Application(
        List<IdValue<Document>> applicationDocuments,
        String applicationSupplier,
        String applicationType,
        String applicationReason,
        String applicationDate,
        String applicationDecision,
        String applicationDecisionReason,
        String applicationDateOfDecision,
        String applicationStatus
    ) {
        this.applicationDocuments = requireNonNull(applicationDocuments);
        this.applicationSupplier = requireNonNull(applicationSupplier);
        this.applicationType = requireNonNull(applicationType);
        this.applicationReason = requireNonNull(applicationReason);
        this.applicationDate = requireNonNull(applicationDate);
        this.applicationDecision = requireNonNull(applicationDecision);
        this.applicationDecisionReason = requireNonNull(applicationDecisionReason);
        this.applicationDateOfDecision = requireNonNull(applicationDateOfDecision);
        this.applicationStatus = requireNonNull(applicationStatus);
    }

    public String getApplicationSupplier() {
        return applicationSupplier;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public String getApplicationReason() {
        return applicationReason;
    }

    public String getApplicationDate() {
        return applicationDate;
    }

    public String getApplicationDecision() {
        return applicationDecision;
    }

    public String getApplicationDecisionReason() {
        return applicationDecisionReason;
    }

    public List<IdValue<Document>> getApplicationDocuments() {
        return applicationDocuments;
    }

    public String getApplicationDateOfDecision() {
        return applicationDateOfDecision;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

}
