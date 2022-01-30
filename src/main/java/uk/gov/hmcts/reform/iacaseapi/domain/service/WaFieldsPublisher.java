package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
public class WaFieldsPublisher {

    private final DateProvider dateProvider;
    private final FeatureToggler featureToggler;

    public WaFieldsPublisher(DateProvider dateProvider, FeatureToggler featureToggler) {
        this.dateProvider = dateProvider;
        this.featureToggler = featureToggler;
    }

    public void addLastModifiedDirection(
            AsylumCase asylumCase, String explanation,
            Parties parties,
            String dateDue,
            DirectionTag tag, String uniqueId, String directionType) {

        if (featureToggler.getValue("publish-wa-fields-feature", false)) {

            final Direction lastModifiedDirection = new Direction(
                    explanation,
                    parties,
                    dateDue,
                    dateProvider.now().toString(),
                    tag,
                    Collections.emptyList(),
                    null,
                    uniqueId,
                    directionType);

            asylumCase.write(AsylumCaseFieldDefinition.LAST_MODIFIED_DIRECTION, lastModifiedDirection);
        }
    }

    public void addLastModifiedApplication(
            AsylumCase asylumCase,
            String applicant,
            String type,
            String details,
            List<IdValue<Document>> evidence,
            String decision,
            String state,
            String applicantRole) {

        System.out.println("Evaluate wa-R2-feature: " + featureToggler.getValue("wa-R2-feature", false));

        if (featureToggler.getValue("wa-R2-feature", false)) {

            final MakeAnApplication lastModifiedApplication = new MakeAnApplication(
                    applicant, type, details,
                    evidence, dateProvider.now().toString(), decision,
                    state);

            System.out.println("lastModifiedApplication type: " + lastModifiedApplication.getType());

            lastModifiedApplication.setApplicantRole(applicantRole);

            asylumCase.write(AsylumCaseFieldDefinition.LAST_MODIFIED_APPLICATION, lastModifiedApplication);
        }
    }
}
