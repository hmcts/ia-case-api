package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.FtpaDecisionCheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class FtpaApplications {

    private String ftpaApplicant;
    private YesOrNo isFtpaNoticeOfDecisionSetAside;
    private String ftpaDecisionObjections;
    private String ftpaDecisionLstIns;
    private List<IdValue<DocumentWithDescription>> ftpaNoticeDocument;
    private String ftpaOutOfTimeExplanation;
    private List<IdValue<DocumentWithDescription>> ftpaOutOfTimeDocuments;
    private List<IdValue<DocumentWithDescription>> ftpaGroundsDocuments;
    private List<IdValue<DocumentWithDescription>> ftpaEvidenceDocuments;
    private String ftpaApplicationDate;
    private String ftpaDecisionOutcomeType;
    private String ftpaDecisionOutcomeTypeR35;
    private String ftpaDecisionRemadeRule32;
    private String ftpaDecisionRemadeRule32Text;
    private List<IdValue<DocumentWithDescription>> ftpaLegacyDecisionDocument;
    private Document ftpaNewDecisionDocument;
    private Document ftpaR35Document;
    private FtpaDecisionCheckValues<String> ftpaDecisionNotesPoints;
    private String ftpaDecisionNotesDescription;
    private String ftpaDecisionDate;
    private String ftpaAppellantGroundsText;

    private FtpaApplications() {
        // noop -- for deserializer
    }
}
