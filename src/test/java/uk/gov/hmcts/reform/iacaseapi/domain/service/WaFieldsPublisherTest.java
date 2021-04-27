package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;

@ExtendWith(MockitoExtension.class)
class WaFieldsPublisherTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private FeatureToggler featureToggler;

    private WaFieldsPublisher waFieldsPublisher;

    @BeforeEach
    public void setup() {
        waFieldsPublisher = new WaFieldsPublisher(dateProvider, featureToggler);
    }

    @Test
    void should_write_field_to_asylum_case() {

        waFieldsPublisher.addLastModifiedDirection(asylumCase, "explanation", Parties.APPELLANT, "22-05-2022", DirectionTag.REQUEST_RESPONSE_REVIEW);
    }


}
