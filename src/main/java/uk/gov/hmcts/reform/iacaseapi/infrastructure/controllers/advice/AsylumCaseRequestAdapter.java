package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.advice;

import java.lang.reflect.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;


@ControllerAdvice
@SuppressWarnings("unchecked")
public class AsylumCaseRequestAdapter extends RequestBodyAdviceAdapter {

    private Logger log = LoggerFactory.getLogger(AsylumCaseRequestAdapter.class);

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {

        Callback<AsylumCase> callback = (Callback<AsylumCase>) body;
        CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();

        RequestContextHolder.currentRequestAttributes().setAttribute("CCDCaseId", caseDetails.getId(), RequestAttributes.SCOPE_REQUEST);

        return body;
    }
}
