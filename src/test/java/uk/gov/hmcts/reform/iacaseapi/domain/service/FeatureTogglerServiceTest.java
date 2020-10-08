package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.mockito.Mockito.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class FeatureTogglerServiceTest {

    @Mock private FeatureToggler featureToggler;

    private FeatureTogglerService featureTogglerService;

    @Before
    public void setUp() {

        featureTogglerService = new FeatureTogglerService(featureToggler);
    }

    @Test
    public void test_true_for_make_an_application_feature_toggle() {

        when(featureToggler.getValue("make-an-application-feature", false)).thenReturn(true);
        Assert.assertEquals(true, featureTogglerService.getValueForMakeAnApplicationFeature());
    }

    @Test
    public void test_false_for_make_an_application_feature_toggle() {

        when(featureToggler.getValue("make-an-application-feature", false)).thenReturn(false);
        Assert.assertEquals(false, featureTogglerService.getValueForMakeAnApplicationFeature());
    }

}
