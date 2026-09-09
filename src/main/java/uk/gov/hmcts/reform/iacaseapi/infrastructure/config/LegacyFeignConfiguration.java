package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Configuration;

/**
 * ccd-case-document-am-client's FeignSupportConfig#multipartFormEncoder is compiled
 * against the removed org.springframework.boot.autoconfigure.http.HttpMessageConverters
 * class, so simply instantiating it throws TypeNotPresentException under Boot 4.0.
 * We remove the bean definition here; FeignConfiguration#feignFormEncoder (also part
 * of defaultConfiguration) provides the same multipart-encoding capability instead.
 */
@Configuration
public class LegacyFeignConfiguration implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        if (registry.containsBeanDefinition("multipartFormEncoder")) {
            registry.removeBeanDefinition("multipartFormEncoder");
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // no-op, required by the interface
    }
}
