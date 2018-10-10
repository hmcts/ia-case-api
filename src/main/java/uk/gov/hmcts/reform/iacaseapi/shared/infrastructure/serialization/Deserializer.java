package uk.gov.hmcts.reform.iacaseapi.shared.infrastructure.serialization;

public interface Deserializer<T> {

    T deserialize(String source);
}
