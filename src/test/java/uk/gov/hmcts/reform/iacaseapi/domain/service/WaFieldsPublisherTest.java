package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class WaFieldsPublisherTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private FeatureToggler featureToggler;

    private WaFieldsPublisher waFieldsPublisher;
    private String uniqueId = UUID.randomUUID().toString();
    private String directionType = "someEventDirectionType";

    @BeforeEach
    public void setup() {
        waFieldsPublisher = new WaFieldsPublisher(dateProvider, featureToggler);
    }

    @Test
    void should_write_field_to_asylum_case() {

        when(featureToggler.getValue("publish-wa-fields-feature", false)).thenReturn(true);
        when(featureToggler.getValue("wa-R2-feature", false)).thenReturn(true);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        waFieldsPublisher.addLastModifiedDirection(asylumCase, "explanation", Parties.APPELLANT, "22-05-2022", DirectionTag.REQUEST_RESPONSE_REVIEW, uniqueId, directionType);
        verify(asylumCase).write(eq(AsylumCaseFieldDefinition.LAST_MODIFIED_DIRECTION), any());
        waFieldsPublisher.addLastModifiedApplication(asylumCase, "Legal representative", "Adjourn",
                "Some application text", Collections.emptyList(), "Pending", "LISTING", "caseworker-ia-legalrep-solicitor");
        verify(asylumCase).write(eq(AsylumCaseFieldDefinition.LAST_MODIFIED_DIRECTION), any());
    }

    @Test
    void should_not_write_field_to_asylum_case_when_flag_is_off() {

        when(featureToggler.getValue("publish-wa-fields-feature", false)).thenReturn(false);
        when(featureToggler.getValue("wa-R2-feature", false)).thenReturn(false);
        waFieldsPublisher.addLastModifiedDirection(asylumCase, "explanation", Parties.APPELLANT, "22-05-2022", DirectionTag.REQUEST_RESPONSE_REVIEW, uniqueId, directionType);
        waFieldsPublisher.addLastModifiedApplication(asylumCase, "Legal representative", "Adjourn",
                "Some application text", Collections.emptyList(), "Pending", "LISTING", "caseworker-ia-legalrep-solicitor");
        verifyNoInteractions(asylumCase);
    }
}
