package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.Collections;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;


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
            DirectionTag tag) {


        if (featureToggler.getValue("publish-wa-fields-feature", false)) {

            final Direction lastModifiedDirection = new Direction(
                    explanation,
                    parties,
                    dateDue,
                    dateProvider.now().toString(),
                    tag,
                    Collections.emptyList());

            asylumCase.write(AsylumCaseFieldDefinition.LAST_MODIFIED_DIRECTION, lastModifiedDirection);
        }
    }
}
