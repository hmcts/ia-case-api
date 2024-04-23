package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PREV_JOURNEY_TYPE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HandlerUtils {

    private HandlerUtils() {
    }

    public static boolean isAipJourney(AsylumCase asylumCase) {
        return asylumCase.read(JOURNEY_TYPE, JourneyType.class)
            .map(journeyType -> journeyType == JourneyType.AIP)
            .orElse(false);
    }

    public static boolean isRepJourney(AsylumCase asylumCase) {
        return asylumCase.read(JOURNEY_TYPE, JourneyType.class)
            .map(journeyType -> journeyType == JourneyType.REP)
            .orElse(true);
    }

    public static boolean isRepToAipJourney(AsylumCase asylumCase) {
        return (asylumCase.read(PREV_JOURNEY_TYPE, JourneyType.class).orElse(null) == JourneyType.REP)
            && isAipJourney(asylumCase);
    }

    public static boolean isAipToRepJourney(AsylumCase asylumCase) {
        return (asylumCase.read(PREV_JOURNEY_TYPE, JourneyType.class).orElse(null) == JourneyType.AIP)
            && isRepJourney(asylumCase);
    }

    public static List<String> readJsonFileList(String filePath, String key) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ClassPathResource fileResource = new ClassPathResource(filePath);
        InputStream file = fileResource.getInputStream();

        JsonNode rootNode = objectMapper.readTree(file);

        JsonNode listNode = rootNode.get(key);
        List<String> valueList = new ArrayList<>();

        if (listNode != null && listNode.isArray()) {
            Iterator<JsonNode> elements = listNode.elements();
            while (elements.hasNext()) {
                JsonNode element = elements.next();
                valueList.add(element.asText());
            }
        }

        return valueList;
    }
}
