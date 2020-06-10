package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

import static java.util.Objects.requireNonNull;

public class HomeOfficeCaseStatus {

    private Person person;
    private ApplicationStatus applicationStatus;
    //Added for CCD case data display
    private String displayDateOfBirth;
    private String displayRejectionReasons;
    private String displayDecisionDate;
    private String displayDecisionSentDate;
    private String displayMetadataValueBoolean;
    private String displayMetadataValueDateTime;

    public HomeOfficeCaseStatus(Person person, ApplicationStatus applicationStatus) {
        this.person = person;
        this.applicationStatus = applicationStatus;
    }

    public HomeOfficeCaseStatus(Person person,
                                ApplicationStatus applicationStatus,
                                String displayDateOfBirth,
                                String displayRejectionReasons,
                                String displayDecisionDate,
                                String displayDecisionSentDate,
                                String displayMetadataValueBoolean,
                                String displayMetadataValueDateTime) {
        this.person = person;
        this.applicationStatus = applicationStatus;
        this.displayDateOfBirth = displayDateOfBirth;
        this.displayRejectionReasons = displayRejectionReasons;
        this.displayDecisionDate = displayDecisionDate;
        this.displayDecisionSentDate = displayDecisionSentDate;
        this.displayMetadataValueBoolean = displayMetadataValueBoolean;
        this.displayMetadataValueDateTime = displayMetadataValueDateTime;
    }

    private HomeOfficeCaseStatus() {

    }

    public Person getPerson() {
        requireNonNull(person);
        return person;
    }

    public ApplicationStatus getApplicationStatus() {
        requireNonNull(applicationStatus);
        return applicationStatus;
    }

    public String getDisplayDateOfBirth() {
        return displayDateOfBirth;
    }

    public String getDisplayRejectionReasons() {
        return displayRejectionReasons;
    }

    public String getDisplayDecisionDate() {
        return displayDecisionDate;
    }

    public String getDisplayDecisionSentDate() {
        return displayDecisionSentDate;
    }

    public String getDisplayMetadataValueBoolean() {
        return displayMetadataValueBoolean;
    }

    public String getDisplayMetadataValueDateTime() {
        return displayMetadataValueDateTime;
    }

}
