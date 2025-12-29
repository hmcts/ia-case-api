package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;

@Component
public class EditDocsAuditService {
    public List<String> getUpdatedAndDeletedDocIdsForGivenField(BailCase bailCase, BailCase bailCaseBefore,
                                                                BailCaseFieldDefinition field) {
        List<IdValue<DocumentWithMetadata>> doc = getDocField(bailCase, field);
        List<IdValue<DocumentWithMetadata>> docBefore = getDocField(bailCaseBefore, field);
        docBefore.removeAll(doc);
        List<String> docIds = new ArrayList<>();
        docBefore.forEach(d -> docIds.add(getIdFromDocUrl(d.getValue().getDocument().getDocumentUrl())));
        return docIds;
    }

    public List<String> getUpdatedAndDeletedDocNamesForGivenField(BailCase bailCase, BailCase bailCaseBefore,
                                                                  BailCaseFieldDefinition field) {
        List<IdValue<DocumentWithMetadata>> doc = getDocField(bailCase, field);
        List<IdValue<DocumentWithMetadata>> docBefore = getDocField(bailCaseBefore, field);

        for (IdValue<DocumentWithMetadata> doc1 : docBefore) {
            if (StringUtils.isBlank(doc1.getValue().getSuppliedBy())) {
                doc1.getValue().setSuppliedBy(null);
            }
        }

        docBefore.removeAll(doc);

        List<String> docNames = new ArrayList<>();
        docBefore.forEach(d -> docNames.add(d.getValue().getDocument().getDocumentFilename()));
        return docNames;
    }

    public List<String> getAddedDocNamesForGivenField(BailCase bailCase, BailCase bailCaseBefore,
                                                      BailCaseFieldDefinition field) {
        List<IdValue<DocumentWithMetadata>> doc = getDocField(bailCase, field);
        List<IdValue<DocumentWithMetadata>> docBefore = getDocField(bailCaseBefore, field);

        List<IdValue<DocumentWithMetadata>> addedDocs = removeDocsWithSameId(doc, docBefore);

        List<String> docNames = new ArrayList<>();
        addedDocs.forEach(d -> docNames.add(d.getValue().getDocument().getDocumentFilename()));
        return docNames;
    }

    public List<String> getAddedDocIdsForGivenField(BailCase bailCase, BailCase bailCaseBefore,
                                                    BailCaseFieldDefinition field) {
        List<IdValue<DocumentWithMetadata>> doc = getDocField(bailCase, field);
        List<IdValue<DocumentWithMetadata>> docBefore = getDocField(bailCaseBefore, field);

        List<IdValue<DocumentWithMetadata>> addedDocs = removeDocsWithSameId(doc, docBefore);

        List<String> docIds = new ArrayList<>();
        addedDocs.forEach(d -> docIds.add(getIdFromDocUrl(d.getValue().getDocument().getDocumentUrl())));
        return docIds;
    }

    public static String getIdFromDocUrl(String documentUrl) {
        String regexToGetStringFromTheLastForwardSlash = "([^/]+$)";
        Pattern pattern = Pattern.compile(regexToGetStringFromTheLastForwardSlash);
        Matcher matcher = pattern.matcher(documentUrl);
        if (matcher.find()) {
            return matcher.group();
        }
        return documentUrl;
    }

    private List<IdValue<DocumentWithMetadata>> getDocField(BailCase bailCase, BailCaseFieldDefinition field) {
        return bailCase.<List<IdValue<DocumentWithMetadata>>>read(field).orElse(Collections.emptyList());
    }

    private List<IdValue<DocumentWithMetadata>> removeDocsWithSameId(List<IdValue<DocumentWithMetadata>> minuend,
                                                                     List<IdValue<DocumentWithMetadata>> subtrahend) {
        List<String> subtrahendIds = subtrahend.stream()
            .map(IdValue::getId)
            .collect(Collectors.toList());

        return minuend.stream()
            .filter(idValue -> !subtrahendIds.contains(idValue.getId()))
            .collect(Collectors.toList());
    }
}
