package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.ss.usermodel.*;

public class CcdDefinitionConverter {


    private final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        if (args == null || args.length < 1) {
            System.out.println("Usage: ./gradlew ccdToJson --args=\"[file1]\"");
            return;
        }

        String json = new CcdDefinitionConverter().toJsonString(args[0]);

        System.out.println(json);
    }


    public String toJsonString(String fileName) {

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(toMap(fileName));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    Map<String, Map<String, List<Map<Integer, List<String>>>>> toMap(String fileName) {

        try (Workbook workbook = WorkbookFactory.create(new File(fileName))) {

            DataFormatter dataFormatter = new DataFormatter();
            dataFormatter.addFormat("mm/dd/yyyyy", new java.text.SimpleDateFormat("MM/dd/yyyy"));
            dataFormatter.addFormat("m/d/yy", new java.text.SimpleDateFormat("MM/dd/yyyy"));

            Map<String, Map<String, List<Map<Integer, List<String>>>>> definitions = new LinkedHashMap<>();

            workbook.forEach(sheet -> {
                Map<String, List<Map<Integer, List<String>>>> s = new LinkedHashMap<>();

                for (int i = 0; i < 3; i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        continue;
                    }
                    if (!s.containsKey("headers")) {
                        // 1st row is always header info
                        s.put("headers", generateHeaderRow(row, dataFormatter));
                    } else if (!s.containsKey("comments")) {
                        // 2nd row are comments to the columns
                        s.put("comments", generateHeaderRow(row, dataFormatter));
                    } else if (!s.containsKey("colNames")) {
                        // 3rd row are columns names
                        s.put("colNames", generateHeaderRow(row, dataFormatter));
                    }
                }

                // starts from 4 corresponds to Excel row number
                s.put("rows", new LinkedList<>());
                AtomicInteger rowIndex = new AtomicInteger(4);
                for (int i = 3; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        continue;
                    }

                    Map<Integer, List<String>> rowWithIndex = new LinkedHashMap<>();
                    rowWithIndex.put(rowIndex.getAndIncrement(), generateRow(row, dataFormatter));
                    s.get("rows").add(rowWithIndex);
                }

                definitions.put(sheet.getSheetName(), s);
            });

            return definitions;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Map<Integer, List<String>>> generateHeaderRow(Row row, DataFormatter dataFormatter) {
        return Lists.newArrayList(
            new ImmutableMap.Builder<Integer, List<String>>()
                .put(row.getRowNum() + 1, generateRow(row, dataFormatter))
                .build()
        );
    }

    private List<String> generateRow(Row row, DataFormatter dataFormatter) {
        List<String> r = new LinkedList<>();
        for (int j = 0; j <= row.getLastCellNum(); j++) {
            Cell cell = row.getCell(j);
            r.add(cell == null ? "" : dataFormatter.formatCellValue(cell));
        }
        return r;
    }
}
