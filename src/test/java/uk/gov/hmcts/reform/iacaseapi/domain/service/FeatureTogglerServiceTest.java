package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FeatureTogglerServiceTest {

    @Mock private FeatureToggler featureToggler;

    private FeatureTogglerService featureTogglerService;

    @Before
    public void setUp() {

        featureTogglerService = new FeatureTogglerService(featureToggler);
    }

    @Test
    public void should_return_true_make_an_application_feature() {
        when(featureToggler.getValue("make-an-application-feature", false))
            .thenReturn(true);
        assertEquals(true, featureTogglerService.getValueForMakeAnApplicationFeature());
    }

}
