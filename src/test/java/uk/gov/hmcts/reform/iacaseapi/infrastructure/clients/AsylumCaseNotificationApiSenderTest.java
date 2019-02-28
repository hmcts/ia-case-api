package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AsylumCaseNotificationApiSenderTest {

    private static final String ENDPOINT = "http://endpoint";
    private static final String ABOUT_TO_SUBMIT_PATH = "/path";

    @Mock private AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;
    @Mock private Callback<AsylumCase> callback;

    private AsylumCaseDocumentApiGenerator asylumCaseDocumentApiGenerator;

    @Before
    public void setUp() {

        asylumCaseDocumentApiGenerator =
            new AsylumCaseDocumentApiGenerator(
                asylumCaseCallbackApiDelegator,
                ENDPOINT,
                ABOUT_TO_SUBMIT_PATH
            );
    }

    @Test
    public void should_delegate_callback_to_downstream_api() {

        final AsylumCase notifiedAsylumCase = mock(AsylumCase.class);

        when(asylumCaseCallbackApiDelegator.delegate(callback, ENDPOINT + ABOUT_TO_SUBMIT_PATH))
            .thenReturn(notifiedAsylumCase);

        final AsylumCase actualAsylumCase = asylumCaseDocumentApiGenerator.generate(callback);

        verify(asylumCaseCallbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + ABOUT_TO_SUBMIT_PATH);

        assertEquals(notifiedAsylumCase, actualAsylumCase);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> asylumCaseDocumentApiGenerator.generate(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
