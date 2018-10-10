package uk.gov.hmcts.reform.iacaseapi.shared.infrastructure.serialization;

public interface Serializer<T> {

    String serialize(T data);
}
