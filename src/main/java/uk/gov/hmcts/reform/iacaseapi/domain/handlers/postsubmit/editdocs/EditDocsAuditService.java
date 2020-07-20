package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.editdocs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.HasDocument;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Component
public class EditDocsAuditService {
    public List<String> getUpdatedAndDeletedDocIdsForGivenField(AsylumCase asylumCase, AsylumCase asylumCaseBefore,
                                                                AsylumCaseFieldDefinition field) {
        List<IdValue<HasDocument>> doc = getDocField(asylumCase, field);
        List<IdValue<HasDocument>> docBefore = getDocField(asylumCaseBefore, field);
        docBefore.removeAll(doc);
        List<String> docIds = new ArrayList<>();
        docBefore.forEach(d -> docIds.add(getIdFromDocUrl(d.getValue().getDocument().getDocumentUrl())));
        return docIds;
    }

    public List<String> getUpdatedAndDeletedDocNamesForGivenField(AsylumCase asylumCase, AsylumCase asylumCaseBefore,
                                                                AsylumCaseFieldDefinition field) {
        List<IdValue<HasDocument>> doc = getDocField(asylumCase, field);
        List<IdValue<HasDocument>> docBefore = getDocField(asylumCaseBefore, field);
        docBefore.removeAll(doc);
        List<String> docIds = new ArrayList<>();
        docBefore.forEach(d -> docIds.add(getIdFromDocUrl(d.getValue().getDocument().getDocumentFilename())));
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

    private List<IdValue<HasDocument>> getDocField(AsylumCase asylumCase, AsylumCaseFieldDefinition field) {
        Optional<List<IdValue<HasDocument>>> doc = asylumCase.read(field);
        return doc.orElse(Collections.emptyList());
    }
}
