package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class CcdDefinitionComparator {

    private final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.out.println("Usage: ./gradlew ccdToCompare --args=\"[file1] [file2]\"");
            return;
        }

        String compareResults = new CcdDefinitionComparator().compare(args[0], args[1]);

        System.out.println(compareResults);
    }

    public String compare(String leftFileName, String rightFileName) {
        try {
            CcdDefinitionConverter converter = new CcdDefinitionConverter();

            Map<String, Map<String, List<Map<Integer, List<String>>>>> leftCcd = converter.toMap(leftFileName);
            Map<String, Map<String, List<Map<Integer, List<String>>>>> rightCcd = converter.toMap(rightFileName);

            Map<String, Map<Integer, String>> added = new LinkedHashMap<>();
            Map<String, Map<Integer, String>> removed = new LinkedHashMap<>();

            rightCcd.keySet().forEach(sheet -> {
                if (!leftCcd.containsKey(sheet)) {
                    // add all rows as new sheet is added
                    added.put(sheet, getContentAsBiMap(sheet, rightCcd));
                    return;
                }

                BiMap<Integer, String> leftContentWithIndex = getContentAsBiMap(sheet, leftCcd);
                BiMap<Integer, String> rightContentWithIndex = getContentAsBiMap(sheet, rightCcd);

                Set<String> leftContent = leftContentWithIndex.entrySet().stream()
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toSet());
                Set<String> rightContent = rightContentWithIndex.entrySet().stream()
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toSet());

                Set<String> addedDiff = Sets.difference(rightContent, leftContent);
                if (!addedDiff.isEmpty()) {
                    added.put(sheet, addedDiff.stream()
                        .collect(Collectors.toMap(diff -> rightContentWithIndex.inverse().get(diff), Function.identity()))
                    );
                }

                Set<String> removedDiff = Sets.difference(leftContent, rightContent);
                if (!removedDiff.isEmpty()) {
                    removed.put(sheet, removedDiff.stream()
                        .collect(Collectors.toMap(diff -> leftContentWithIndex.inverse().get(diff), Function.identity()))
                    );
                }
            });

            leftCcd.keySet().forEach(sheet -> {
                if (!rightCcd.containsKey(sheet)) {
                    // add all rows as the sheet was removed
                    removed.put(sheet, getContentAsBiMap(sheet, leftCcd));
                }
            });

            return "Added:\n"
                   + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(added)
                   + "\n\nRemoved:\n"
                   + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(removed);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private BiMap<Integer, String> getContentAsBiMap(String sheet, Map<String, Map<String, List<Map<Integer, List<String>>>>> ccd) {

        return HashBiMap.create(
            ccd.get(sheet).get("rows").stream()
                .collect(Collectors.toMap(row -> row.keySet().iterator().next(), row -> Joiner.on(", ").join(row.values().iterator().next())))
                .entrySet()
                .stream()
                .filter(entry -> !StringUtils.isAllBlank(entry.getValue().replace(",", "")))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }
}
