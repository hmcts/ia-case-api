package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private CorrelationIdFilter correlationIdFilter;

    @BeforeEach
    void setUp() {
        correlationIdFilter = new CorrelationIdFilter();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void should_use_existing_correlation_id_from_header() throws Exception {
        String existingCorrelationId = "existing-correlation-id-123";
        when(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER))
            .thenReturn(existingCorrelationId);

        correlationIdFilter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, existingCorrelationId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_generate_new_correlation_id_when_header_is_missing() throws Exception {
        when(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn(null);

        correlationIdFilter.doFilterInternal(request, response, filterChain);

        ArgumentCaptor<String> correlationIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(
            org.mockito.ArgumentMatchers.eq(CorrelationIdFilter.CORRELATION_ID_HEADER),
            correlationIdCaptor.capture()
        );

        String generatedCorrelationId = correlationIdCaptor.getValue();
        assertNotNull(generatedCorrelationId);
        // UUID format: 8-4-4-4-12 characters
        assertEquals(36, generatedCorrelationId.length());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_generate_new_correlation_id_when_header_is_blank() throws Exception {
        when(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn("   ");

        correlationIdFilter.doFilterInternal(request, response, filterChain);

        ArgumentCaptor<String> correlationIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(
            org.mockito.ArgumentMatchers.eq(CorrelationIdFilter.CORRELATION_ID_HEADER),
            correlationIdCaptor.capture()
        );

        String generatedCorrelationId = correlationIdCaptor.getValue();
        assertNotNull(generatedCorrelationId);
        assertEquals(36, generatedCorrelationId.length());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_clear_mdc_after_filter_chain_completes() throws Exception {
        String correlationId = "test-correlation-id";
        when(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn(correlationId);

        correlationIdFilter.doFilterInternal(request, response, filterChain);

        assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
        assertNull(MDC.get(CorrelationIdFilter.CCD_CASE_ID_MDC_KEY));
    }

    @Test
    void should_clear_mdc_even_when_filter_chain_throws_exception() throws Exception {
        String correlationId = "test-correlation-id";
        when(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn(correlationId);

        RuntimeException expectedException = new RuntimeException("Filter chain error");
        org.mockito.Mockito.doThrow(expectedException).when(filterChain).doFilter(request, response);

        try {
            correlationIdFilter.doFilterInternal(request, response, filterChain);
        } catch (RuntimeException e) {
            assertEquals(expectedException, e);
        }

        assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
        assertNull(MDC.get(CorrelationIdFilter.CCD_CASE_ID_MDC_KEY));
    }
}
