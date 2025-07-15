package uk.gov.hmcts.reform.iacaseapi;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

public class SimpleRequestLoggingFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
        // No-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        System.out.println("Request URI: " + request.getRemoteAddr());
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No-op
    }
}
