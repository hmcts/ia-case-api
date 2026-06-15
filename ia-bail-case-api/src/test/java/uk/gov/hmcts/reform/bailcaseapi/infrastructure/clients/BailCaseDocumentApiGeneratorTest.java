package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;

@ExtendWith(MockitoExtension.class)
public class BailCaseDocumentApiGeneratorTest {
    private static final String ENDPOINT = "http://endpoint";
    private static final String ABOUT_TO_SUBMIT_PATH = "/path";
    private static final String ABOUT_TO_START_PATH = "/path";

    @Mock
    private BailCaseCallbackApiDelegator bailCaseCallbackApiDelegator;
    @Mock
    private Callback<BailCase> callback;

    private BailCaseDocumentApiGenerator bailCaseDocumentApiGenerator;

    @BeforeEach
    public void setUp() {

        bailCaseDocumentApiGenerator =
            new BailCaseDocumentApiGenerator(
                bailCaseCallbackApiDelegator,
                ENDPOINT,
                ABOUT_TO_SUBMIT_PATH,
                ABOUT_TO_START_PATH
            );
    }

    @Test
    void should_delegate_callback_to_downstream_api() {

        final BailCase submittedBailCase = mock(BailCase.class);

        when(bailCaseCallbackApiDelegator.delegate(callback, ENDPOINT + ABOUT_TO_SUBMIT_PATH))
            .thenReturn(submittedBailCase);

        final BailCase actualBailCase = bailCaseDocumentApiGenerator.generate(callback);

        verify(bailCaseCallbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + ABOUT_TO_SUBMIT_PATH);

        assertEquals(submittedBailCase, actualBailCase);
    }

    @Test
    public void should_delegate_about_to_start_callback_to_downstream_api() {

        final BailCase submittedBailCase = mock(BailCase.class);

        when(bailCaseCallbackApiDelegator.delegate(callback, ENDPOINT + ABOUT_TO_START_PATH))
            .thenReturn(submittedBailCase);

        final BailCase actualBailCase = bailCaseDocumentApiGenerator.aboutToStart(callback);

        verify(bailCaseCallbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + ABOUT_TO_START_PATH);

        assertEquals(submittedBailCase, actualBailCase);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> bailCaseDocumentApiGenerator.generate(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
