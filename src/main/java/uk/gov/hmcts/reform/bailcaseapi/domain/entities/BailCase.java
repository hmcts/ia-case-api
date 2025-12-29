package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.util.HashMap;
import java.util.Optional;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseData;

public class BailCase extends HashMap<String, Object> implements CaseData, uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public BailCase() {
        objectMapper.registerModule(new Jdk8Module());
    }

    public <T> Optional<T> read(BailCaseFieldDefinition extractor, Class<T> type) {
        return this.read(extractor);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> read(BailCaseFieldDefinition extractor) {

        Object o = this.get(extractor.value());

        if (o == null) {
            return Optional.empty();
        }

        Object value = objectMapper.convertValue(o, extractor.getTypeReference());

        return Optional.of((T) value);
    }

    public <T> void write(BailCaseFieldDefinition extractor, T value) {
        this.put(extractor.value(), value);
    }

    public void clear(BailCaseFieldDefinition extractor) {
        this.put(extractor.value(), null);
    }

    public void remove(BailCaseFieldDefinition field) {
        this.remove(field.value());
    }

    public void removeByString(String field) {
        this.remove(field);
    }
}
