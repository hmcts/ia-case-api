package uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization;

public interface Deserializer<T> {

    T deserialize(String source);
}
