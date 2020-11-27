package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRole;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
public class MakeAnApplicationAppender {

    private final UserDetails userDetails;
    private final DateProvider dateProvider;

    public MakeAnApplicationAppender(UserDetails userDetails, DateProvider dateProvider) {
        this.userDetails = userDetails;
        this.dateProvider = dateProvider;
    }

    public List<IdValue<MakeAnApplication>> append(
        List<IdValue<MakeAnApplication>> existingMakeAnApplications,
        String makeAnApplicationType,
        String makeAnApplicationDesc,
        List<IdValue<Document>> makeAnApplicationEvidence,
        String makeAnApplicationDecision,
        String makeAnApplicationState
    ) {

        requireNonNull(existingMakeAnApplications);
        requireNonNull(makeAnApplicationType);
        requireNonNull(makeAnApplicationDesc);
        requireNonNull(makeAnApplicationEvidence);
        requireNonNull(makeAnApplicationDecision);
        requireNonNull(makeAnApplicationState);

        Stream<UserRole> allowedRoles = Arrays.stream(UserRole.values());
        UserRole applicantRole =  allowedRoles
            .filter(r -> userDetails.getRoles().contains(r.toString()))
            .findAny()
            .orElseThrow(() -> new IllegalStateException("No valid user role is present."));

        String applicant = getApplicantType(applicantRole);

        final MakeAnApplication newMakeAnApplication =
            new MakeAnApplication(applicant, makeAnApplicationType, makeAnApplicationDesc,
                makeAnApplicationEvidence, dateProvider.now().toString(), makeAnApplicationDecision,
                makeAnApplicationState);
        newMakeAnApplication.setApplicantRole(applicantRole.toString());

        final List<IdValue<MakeAnApplication>> allMakeAnApplications =
            new ArrayList<>();

        int index = existingMakeAnApplications.size() + 1;

        allMakeAnApplications.add(new IdValue<>(String.valueOf(index--), newMakeAnApplication));

        for (IdValue<MakeAnApplication> existingMakeAnApplication : existingMakeAnApplications) {
            allMakeAnApplications.add(new IdValue<>(String.valueOf(index--), existingMakeAnApplication.getValue()));
        }

        return allMakeAnApplications;
    }

    private String getApplicantType(UserRole userRole) {

        switch (userRole) {
            case HOME_OFFICE_APC:
            case HOME_OFFICE_POU:
            case HOME_OFFICE_LART:
            case HOME_OFFICE_GENERIC:
                return "Respondent";

            case LEGAL_REPRESENTATIVE:
                return "Legal representative";

            default:
                throw new IllegalStateException("Unauthorized role to make an application");
        }
    }
}
