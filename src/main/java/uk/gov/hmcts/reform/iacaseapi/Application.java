package uk.gov.hmcts.reform.iacaseapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;

@SpringBootApplication
@EnableCircuitBreaker
@SuppressWarnings(
    {
        "HideUtilityClassConstructor", // Spring needs a constructor, its not a utility class
        "unchecked"
    }
)
public class Application implements CommandLineRunner {

    private static ClassLoader CLASS_LOADER = Thread.currentThread().getContextClassLoader();

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public void run(String... args) throws IOException {

        InputStream templateInputStream =
            CLASS_LOADER.getResourceAsStream("ccd-definition/template.xlsx");

        try (Workbook workbook = new XSSFWorkbook(templateInputStream)) {

            applyAuthorisation(workbook);
            applyJurisdiction(workbook);
            applyAuthorisationCaseType(workbook);
            applyAuthorisationCaseField(workbook);
            applyAuthorisationCaseEvent(workbook);
            applyAuthorisationCaseState(workbook);

            File generatedFile = new File("template_generated.xlsx");
            FileOutputStream generatedFileStream = new FileOutputStream(generatedFile);
            workbook.write(generatedFileStream);
        }

        System.exit(0);
    }

    private void applyAuthorisation(Workbook workbook) {

        final Sheet userProfileSheet = workbook.getSheet("UserProfile");

        NodeLeafMap<String, Object> authorisation = loadJsonIntoMap("ccd-definition/authorisation.json");
        NodeLeafMap<String, NodeLeafMap<String, Object>> users = authorisation.getAsNodeMap("users");

        for (Map.Entry<String, NodeLeafMap<String, Object>> user : users.entrySet()) {

            String emailAddress = user.getKey();

            NodeLeafMap<String, String> workBasketValues =
                user.getValue()
                    .getAsLeafMap("workBasket");

            Row newRow = userProfileSheet.createRow(userProfileSheet.getLastRowNum() + 1);

            newRow.createCell(2).setCellValue(emailAddress);

            if (workBasketValues != null) {

                newRow.createCell(3).setCellValue(workBasketValues.getOrDefault("defaultJurisdiction", ""));
                newRow.createCell(4).setCellValue(workBasketValues.getOrDefault("defaultCaseType", ""));
                newRow.createCell(5).setCellValue(workBasketValues.getOrDefault("defaultState", ""));
            }
        }
    }

    private void applyJurisdiction(Workbook workbook) {

        final Sheet jurisdictionSheet = workbook.getSheet("Jurisdiction");

        NodeLeafMap<String, Object> service = loadJsonIntoMap("ccd-definition/service.json");

        for (Map.Entry<String, NodeLeafMap<String, Object>> jurisdiction : service.getAsNodeMap("jurisdictions").entrySet()) {

            String jurisdictionId = jurisdiction.getKey();
            String jurisdictionName = jurisdiction.getValue().getAsString("name");
            String jurisdictionDescription = jurisdiction.getValue().getAsString("description");

            Row newRow = jurisdictionSheet.createRow(jurisdictionSheet.getLastRowNum() + 1);

            newRow.createCell(2).setCellValue(jurisdictionId);
            newRow.createCell(3).setCellValue(jurisdictionName);
            newRow.createCell(4).setCellValue(jurisdictionDescription);
        }
    }

    private void applyAuthorisationCaseType(Workbook workbook) {

        final Sheet authorisationCaseTypeSheet = workbook.getSheet("AuthorisationCaseType");

        NodeLeafMap<String, Object> service = loadJsonIntoMap("ccd-definition/service.json");

        for (Map.Entry<String, NodeLeafMap<String, Object>> jurisdiction : service.getAsNodeMap("jurisdictions").entrySet()) {
            for (Map.Entry<String, NodeLeafMap<String, Object>> caseType : jurisdiction.getValue().getAsNodeMap("caseTypes").entrySet()) {

                String caseTypeId = caseType.getKey();

                for (Map.Entry<String, String> role : caseType.getValue().getAsLeafMap("roles").entrySet()) {

                    String userRole = role.getKey();
                    String crud = role.getValue();

                    Row newRow = authorisationCaseTypeSheet.createRow(authorisationCaseTypeSheet.getLastRowNum() + 1);

                    newRow.createCell(2).setCellValue(caseTypeId);
                    newRow.createCell(3).setCellValue(userRole);
                    newRow.createCell(4).setCellValue(crud);
                }
            }
        }
    }

    private void applyAuthorisationCaseField(Workbook workbook) {

        final Sheet authorisationCaseFieldSheet = workbook.getSheet("AuthorisationCaseField");

        NodeLeafMap<String, Object> service = loadJsonIntoMap("ccd-definition/service.json");

        for (Map.Entry<String, NodeLeafMap<String, Object>> jurisdiction : service.getAsNodeMap("jurisdictions").entrySet()) {
            for (Map.Entry<String, NodeLeafMap<String, Object>> caseType : jurisdiction.getValue().getAsNodeMap("caseTypes").entrySet()) {

                String caseTypeId = caseType.getKey();

                for (Map.Entry<String, NodeLeafMap<String, Object>> caseField : caseType.getValue().getAsNodeMap("caseFields").entrySet()) {

                    String caseFieldId = caseField.getKey();

                    for (Map.Entry<String, String> role : caseField.getValue().getAsLeafMap("roles").entrySet()) {

                        String userRole = role.getKey();
                        String crud = role.getValue();

                        Row newRow = authorisationCaseFieldSheet.createRow(authorisationCaseFieldSheet.getLastRowNum() + 1);

                        newRow.createCell(2).setCellValue(caseTypeId);
                        newRow.createCell(3).setCellValue(caseFieldId);
                        newRow.createCell(4).setCellValue(userRole);
                        newRow.createCell(5).setCellValue(crud);
                    }
                }
            }
        }
    }

    private void applyAuthorisationCaseEvent(Workbook workbook) {

        final Sheet authorisationCaseEventSheet = workbook.getSheet("AuthorisationCaseEvent");

        NodeLeafMap<String, Object> service = loadJsonIntoMap("ccd-definition/service.json");

        for (Map.Entry<String, NodeLeafMap<String, Object>> jurisdiction : service.getAsNodeMap("jurisdictions").entrySet()) {
            for (Map.Entry<String, NodeLeafMap<String, Object>> caseType : jurisdiction.getValue().getAsNodeMap("caseTypes").entrySet()) {

                String caseTypeId = caseType.getKey();

                for (Map.Entry<String, NodeLeafMap<String, Object>> caseEvent : caseType.getValue().getAsNodeMap("caseEvents").entrySet()) {

                    String caseEventId = caseEvent.getKey();

                    for (Map.Entry<String, String> role : caseEvent.getValue().getAsLeafMap("roles").entrySet()) {

                        String userRole = role.getKey();
                        String crud = role.getValue();

                        Row newRow = authorisationCaseEventSheet.createRow(authorisationCaseEventSheet.getLastRowNum() + 1);

                        newRow.createCell(2).setCellValue(caseTypeId);
                        newRow.createCell(3).setCellValue(caseEventId);
                        newRow.createCell(4).setCellValue(userRole);
                        newRow.createCell(5).setCellValue(crud);
                    }
                }
            }
        }
    }

    private void applyAuthorisationCaseState(Workbook workbook) {

        final Sheet authorisationCaseStateSheet = workbook.getSheet("AuthorisationCaseState");

        NodeLeafMap<String, Object> service = loadJsonIntoMap("ccd-definition/service.json");

        for (Map.Entry<String, NodeLeafMap<String, Object>> jurisdiction : service.getAsNodeMap("jurisdictions").entrySet()) {
            for (Map.Entry<String, NodeLeafMap<String, Object>> caseType : jurisdiction.getValue().getAsNodeMap("caseTypes").entrySet()) {

                String caseTypeId = caseType.getKey();

                for (Map.Entry<String, NodeLeafMap<String, Object>> state : caseType.getValue().getAsNodeMap("states").entrySet()) {

                    String caseStateId = state.getKey();

                    for (Map.Entry<String, String> role : state.getValue().getAsLeafMap("roles").entrySet()) {

                        String userRole = role.getKey();
                        String crud = role.getValue();

                        Row newRow = authorisationCaseStateSheet.createRow(authorisationCaseStateSheet.getLastRowNum() + 1);

                        newRow.createCell(2).setCellValue(caseTypeId);
                        newRow.createCell(3).setCellValue(caseStateId);
                        newRow.createCell(4).setCellValue(userRole);
                        newRow.createCell(5).setCellValue(crud);
                    }
                }
            }
        }
    }

    private NodeLeafMap<String, Object> loadJsonIntoMap(String resource) {

        final ObjectMapper mapper = new ObjectMapper();

        final InputStream authorisationInputStream =
            CLASS_LOADER.getResourceAsStream(resource);

        try {

            return new NodeLeafMap<>(
                mapper.readValue(
                    authorisationInputStream,
                    new TypeReference<Map<String, Object>>() {
                    }
                )
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class NodeLeafMap<K, V> implements Map<K, V> {

        private final Map<K, V> map;

        public NodeLeafMap(Map<K, V> map) {
            this.map = map;
        }

        public NodeLeafMap<String, NodeLeafMap<String, Object>> getAsNodeMap(String key) {

            Map<String, NodeLeafMap<String, Object>> promotedMap = new HashMap<>();

            ((Map<String, Object>) map.get(key))
                .entrySet()
                .stream()
                .forEach(e -> promotedMap.put(
                    e.getKey(),
                    new NodeLeafMap((Map<String, Object>) e.getValue())
                ));

            return new NodeLeafMap(promotedMap);
        }

        public NodeLeafMap<String, String> getAsLeafMap(String key) {
            return new NodeLeafMap<>(
                ((Map<String, String>) map.get(key))
            );
        }

        public boolean isString(String key) {
            return map.get(key) == null || map.get(key) instanceof String;
        }

        public String getAsString(String key) {
            return map.get(key) == null ? "" : map.get(key).toString();
        }

        public int size() {
            return map.size();
        }

        public boolean isEmpty() {
            return map.isEmpty();
        }

        public boolean containsKey(Object key) {
            return map.containsKey(key);
        }

        public boolean containsValue(Object value) {
            return map.containsValue(value);
        }

        public V get(Object key) {
            return map.get(key);
        }

        public V put(K key, V value) {
            return map.put(key, value);
        }

        public V remove(Object key) {
            return map.remove(key);
        }

        public void putAll(Map<? extends K, ? extends V> m) {
            map.putAll(m);
        }

        public void clear() {
            map.clear();
        }

        public Set<K> keySet() {
            return map.keySet();
        }

        public Collection<V> values() {
            return map.values();
        }

        public Set<Entry<K, V>> entrySet() {
            return map.entrySet();
        }
    }
}
