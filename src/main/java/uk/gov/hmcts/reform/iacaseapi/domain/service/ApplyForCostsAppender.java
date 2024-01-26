package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplyForCosts;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@Service
public class ApplyForCostsAppender {
    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;
    private final DateProvider dateProvider;
    private final String legalRepresentative = "Legal representative";
    private final String homeOffice = "Home office";
    private final String respondent = "Respondent";

    public ApplyForCostsAppender(UserDetails userDetails, UserDetailsHelper userDetailsHelper, DateProvider dateProvider) {
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
        this.dateProvider = dateProvider;
    }

    public List<IdValue<ApplyForCosts>> append(
        List<IdValue<ApplyForCosts>> existingAppliesForCosts,
        String applyForCostsDecision,
        String appliedCostsType,
        String tribunalConsideringReason,
        List<IdValue<Document>> judgeEvidenceForCostsOrder,
        String respondentToCostsOrder
    ) {
        requireNonNull(existingAppliesForCosts);
        requireNonNull(applyForCostsDecision);
        requireNonNull(appliedCostsType);
        requireNonNull(tribunalConsideringReason);
        requireNonNull(respondentToCostsOrder);

        final ApplyForCosts newApplyForCosts = new ApplyForCosts(
            applyForCostsDecision,
            appliedCostsType,
            retrieveApplicant(),
            tribunalConsideringReason,
            judgeEvidenceForCostsOrder,
            dateProvider.now().toString(),
            // respondentToCostsOrder used for UI representation only, applyForCostsRespondentRole is used for business logic
            respondentToCostsOrder,
            respondentToCostsOrder
        );

        return getIndexUpdatedAppliesForCosts(existingAppliesForCosts, newApplyForCosts);
    }

    public List<IdValue<ApplyForCosts>> append(
        List<IdValue<ApplyForCosts>> existingAppliesForCosts,
        String appliedCostsType,
        String argumentsAndEvidenceDetails,
        List<IdValue<Document>> argumentsAndEvidenceDocuments,
        List<IdValue<Document>> scheduleOfCostsDocuments,
        YesOrNo applyForCostsHearingType,
        String applyForCostsHearingTypeExplanation,
        String applyForCostsDecision,
        String legalRepName,
        String applyForCostsOotExplanation,
        List<IdValue<Document>> ootUploadEvidenceDocuments,
        YesOrNo isApplyForCostsOot
    ) {

        requireNonNull(existingAppliesForCosts);
        requireNonNull(appliedCostsType);
        requireNonNull(argumentsAndEvidenceDocuments);
        requireNonNull(applyForCostsHearingType);
        requireNonNull(applyForCostsDecision);

        if (applyForCostsHearingType.equals(YesOrNo.YES)) {
            requireNonNull(applyForCostsHearingTypeExplanation);
        }

        requireNonNull(legalRepName);

        if (isApplyForCostsOot.equals(YesOrNo.YES)) {
            requireNonNull(applyForCostsOotExplanation);
        }

        final ApplyForCosts newApplyForCosts = new ApplyForCosts(
            appliedCostsType,
            argumentsAndEvidenceDetails,
            argumentsAndEvidenceDocuments,
            scheduleOfCostsDocuments,
            applyForCostsHearingType,
            applyForCostsHearingTypeExplanation,
            applyForCostsDecision,
            retrieveApplicant(),
            dateProvider.now().toString(),
            resolveRespondentToCostsOrder(retrieveApplicant(), legalRepName),
            applyForCostsOotExplanation,
            ootUploadEvidenceDocuments,
            isApplyForCostsOot,
            resolveRespondentRoleToCostsOrder(retrieveApplicant())
        );

        return getIndexUpdatedAppliesForCosts(existingAppliesForCosts, newApplyForCosts);
    }

    private String retrieveApplicant() {
        String applicant = userDetailsHelper.getLoggedInUserRoleLabel(userDetails).toString();
        if (applicant.equals(respondent)) {
            applicant = homeOffice;
        } else if (userDetailsHelper.getLoggedInUserRoleLabel(userDetails).equals(UserRoleLabel.JUDGE)) {
            applicant = "Tribunal";
        }
        return applicant;
    }

    private List<IdValue<ApplyForCosts>> getIndexUpdatedAppliesForCosts(List<IdValue<ApplyForCosts>> existingAppliesForCosts, ApplyForCosts newApplyForCosts) {
        final List<IdValue<ApplyForCosts>> allAppliesForCosts =
            new ArrayList<>();

        int index = existingAppliesForCosts.size() + 1;

        allAppliesForCosts.add(new IdValue<>(String.valueOf(index--), newApplyForCosts));

        for (IdValue<ApplyForCosts> existingApplyForCosts : existingAppliesForCosts) {
            allAppliesForCosts.add(new IdValue<>(String.valueOf(index--), existingApplyForCosts.getValue()));
        }

        return allAppliesForCosts;
    }

    private String resolveRespondentToCostsOrder(String applicant, String legalRepName) {
        return switch (applicant) {
            case homeOffice -> legalRepName;
            case legalRepresentative -> homeOffice;
            default -> throw new IllegalStateException("Provided applicant is not valid");
        };
    }

    private String resolveRespondentRoleToCostsOrder(String applicant) {
        return switch (applicant) {
            case homeOffice -> legalRepresentative;
            case legalRepresentative -> homeOffice;
            default -> throw new IllegalStateException("Provided applicant is not valid");
        };
    }
}
