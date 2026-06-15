package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;

import java.util.function.Consumer;

public class TimedEventMessageConverterCustomizer<T> implements ObjectProvider<T> {
    @Override
    public T getObject() throws BeansException {
        return null;
    }

    @Override
    public T getObject(Object... args) throws BeansException {
        return null;
    }

    @Override
    public T getIfAvailable() throws BeansException {
        return null;
    }

    @Override
    public T getIfUnique() throws BeansException {
        return null;
    }

    @Override
    public void forEach(Consumer action) {
        // do nothing
    }

}
