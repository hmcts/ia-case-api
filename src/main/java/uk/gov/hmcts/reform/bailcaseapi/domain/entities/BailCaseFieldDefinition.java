package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;

public enum BailCaseFieldDefinition {
    //Example
    APPELLANT_GIVEN_NAMES(
        "appellantGivenNames", new TypeReference<String>() {})
    ;


    private final String value;
    private final TypeReference typeReference;

    BailCaseFieldDefinition(String value, TypeReference typeReference) {
        this.value = value;
        this.typeReference = typeReference;
    }

    public String value() {
        return value;
    }

    public TypeReference getTypeReference() {
        return typeReference;
    }
}
