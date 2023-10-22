package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@Service
public class ApplyForCostsHandlerAppender {
    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;
    private final DateProvider dateProvider;

    public ApplyForCostsHandlerAppender(UserDetails userDetails, UserDetailsHelper userDetailsHelper, DateProvider dateProvider) {
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
        this.dateProvider = dateProvider;
    }

    public List<IdValue<ApplyForCosts>> append(
            List<IdValue<ApplyForCosts>> existingAppliesForCosts,
            TypesOfAppliedCosts typesOfAppliedCosts,
            String argumentsAndEvidenceDetails,
            List<IdValue<Document>> argumentsAndEvidenceDocuments,
            List<IdValue<Document>> scheduleOfCostsDocuments,
            YesOrNo applyForCostsHearingType,
            String applyForCostsHearingTypeExplanation,
            String applyForCostsDecision
    ) {

        requireNonNull(existingAppliesForCosts);
        requireNonNull(typesOfAppliedCosts);
        requireNonNull(argumentsAndEvidenceDocuments);
        requireNonNull(scheduleOfCostsDocuments);
        requireNonNull(applyForCostsHearingType);
        requireNonNull(applyForCostsDecision);
        if (applyForCostsHearingType.equals(YesOrNo.YES)) {
            requireNonNull(applyForCostsHearingTypeExplanation);
        }

        String applicant = userDetailsHelper.getLoggedInUserRoleLabel(userDetails).toString();

        final ApplyForCosts newApplyForCosts = new ApplyForCosts(
                typesOfAppliedCosts,
                argumentsAndEvidenceDetails,
                argumentsAndEvidenceDocuments,
                scheduleOfCostsDocuments,
                applyForCostsHearingType,
                applyForCostsHearingTypeExplanation,
                applyForCostsDecision,
                applicant,
                dateProvider.now().toString()
        );

//        TypesOfAppliedCosts typesOfAppliedCosts,
//        String argumentsAndEvidenceDetails,
//        List<IdValue<Document>> argumentsAndEvidenceDocuments,
//        List<IdValue<Document>> scheduleOfCostsDocuments,
//        YesOrNo applyForCostsHearingType,
//        String applyForCostsHearingTypeExplanation,
//        String applyForCostsDecision

        final List<IdValue<ApplyForCosts>> allAppliesForCosts =
                new ArrayList<>();

        int index = existingAppliesForCosts.size() + 1;

        allAppliesForCosts.add(new IdValue<>(String.valueOf(index--), newApplyForCosts));

        for (IdValue<ApplyForCosts> existingApplyForCosts : existingAppliesForCosts) {
            allAppliesForCosts.add(new IdValue<>(String.valueOf(index--), existingApplyForCosts.getValue()));
        }

        return allAppliesForCosts;
    }
}
