package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import tools.jackson.databind.json.JsonMapper;
import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;

class IdValueMixinTest {

    @Test
    void should_deserialise_id_value_using_mixin() throws Exception {

        ObjectMapper mapper = JsonMapper.builder()
            .addMixIn(IdValue.class, IdValueMixin.class)
            .build();

        String json =
            "[{\"id\":\"123\",\"value\":{\"familyName\":\"Smith\",\"givenNames\":\"John\"}}]";

        List<IdValue<HomeOfficeAppellant>> result =
            mapper.readValue(
                json,
                new TypeReference<
                    List<IdValue<HomeOfficeAppellant>>>() {}
            );

        assertEquals(1, result.size());

        IdValue<HomeOfficeAppellant> idValue =
            result.getFirst();

        assertEquals("123", idValue.getId());

        assertEquals(
            "Smith",
            idValue.getValue().getFamilyName()
        );

        assertEquals(
            "John",
            idValue.getValue().getGivenNames()
        );
    }

    @Test
    void should_serialise_id_value_using_mixin() throws Exception {

        ObjectMapper mapper = JsonMapper.builder()
            .addMixIn(IdValue.class, IdValueMixin.class)
            .build();

        HomeOfficeAppellant appellant =
            new HomeOfficeAppellant();

        appellant.setFamilyName("Smith");
        appellant.setGivenNames("John");

        IdValue<HomeOfficeAppellant> idValue =
            new IdValue<>("123", appellant);

        String json =
            mapper.writeValueAsString(
                List.of(idValue)
            );

        List<IdValue<HomeOfficeAppellant>> result =
            mapper.readValue(
                json,
                new TypeReference<
                    List<IdValue<HomeOfficeAppellant>>>() {}
            );

        assertEquals(1, result.size());

        assertEquals(
            "123",
            result.getFirst().getId()
        );

        assertEquals(
            "Smith",
            result.getFirst().getValue().getFamilyName()
        );
    }
}