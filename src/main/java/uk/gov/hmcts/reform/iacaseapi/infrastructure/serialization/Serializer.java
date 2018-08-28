package uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization;

public interface Serializer<T> {

    String serialize(T data);
}
