package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties.RESPONDENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;


@Component
public class FtpaRespondentHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;
    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;
    private final Appender<FtpaApplications> ftpaAppender;
    private final FeatureToggler featureToggler;

    public FtpaRespondentHandler(
            DateProvider dateProvider,
            DocumentReceiver documentReceiver,
            DocumentsAppender documentsAppender,
            Appender<FtpaApplications> ftpaAppender,
            FeatureToggler featureToggler
    ) {
        this.dateProvider = dateProvider;
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
        this.ftpaAppender = ftpaAppender;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.APPLY_FOR_FTPA_RESPONDENT;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final Optional<List<IdValue<DocumentWithDescription>>>  maybeOutOfTimeDocuments = asylumCase.read(FTPA_RESPONDENT_OUT_OF_TIME_DOCUMENTS);

        final List<IdValue<DocumentWithDescription>> ftpaRespondentOutOfTimeDocuments = maybeOutOfTimeDocuments.orElse(Collections.emptyList());

        final Optional<List<IdValue<DocumentWithDescription>>>  maybeDocument = asylumCase.read(FTPA_RESPONDENT_GROUNDS_DOCUMENTS);

        final List<IdValue<DocumentWithDescription>> ftpaRespondentGrounds = maybeDocument.orElse(Collections.emptyList());

        final Optional<List<IdValue<DocumentWithDescription>>> maybeFtpaRespondentEvidence = asylumCase.read(FTPA_RESPONDENT_EVIDENCE_DOCUMENTS);

        final List<IdValue<DocumentWithDescription>> ftpaRespondentEvidence = maybeFtpaRespondentEvidence.orElse(Collections.emptyList());

        final Optional<List<IdValue<DocumentWithMetadata>>> maybeFtpaRespondentDocuments = asylumCase.read(FTPA_RESPONDENT_DOCUMENTS);

        final List<IdValue<DocumentWithMetadata>> existingFtpaRespondentDocuments = maybeFtpaRespondentDocuments.orElse(Collections.emptyList());

        final List<DocumentWithMetadata> ftpaRespondentDocuments = new ArrayList<>();

        ftpaRespondentDocuments.addAll(
            documentReceiver
                .tryReceiveAll(
                    ftpaRespondentOutOfTimeDocuments,
                    DocumentTag.FTPA_RESPONDENT
                )
        );

        ftpaRespondentDocuments.addAll(
            documentReceiver
                .tryReceiveAll(
                    ftpaRespondentGrounds,
                    DocumentTag.FTPA_RESPONDENT
                )
        );

        ftpaRespondentDocuments.addAll(
            documentReceiver
                .tryReceiveAll(
                    ftpaRespondentEvidence,
                    DocumentTag.FTPA_RESPONDENT
                )
        );

        final List<IdValue<DocumentWithMetadata>> allRespondentDocuments =
            documentsAppender.append(
                existingFtpaRespondentDocuments,
                ftpaRespondentDocuments
            );

        if (maybeDocument.isPresent()) {
            asylumCase.write(IS_FTPA_RESPONDENT_GROUNDS_DOCS_VISIBLE_IN_SUBMITTED, YES);
            asylumCase.write(IS_FTPA_RESPONDENT_GROUNDS_DOCS_VISIBLE_IN_DECIDED, NO);
        }
        if (maybeFtpaRespondentEvidence.isPresent()) {
            asylumCase.write(IS_FTPA_RESPONDENT_EVIDENCE_DOCS_VISIBLE_IN_SUBMITTED, YES);
            asylumCase.write(IS_FTPA_RESPONDENT_EVIDENCE_DOCS_VISIBLE_IN_DECIDED, NO);
        }
        if (maybeOutOfTimeDocuments.isPresent()) {
            asylumCase.write(IS_FTPA_RESPONDENT_OOT_DOCS_VISIBLE_IN_SUBMITTED, YES);
            asylumCase.write(IS_FTPA_RESPONDENT_OOT_DOCS_VISIBLE_IN_DECIDED, NO);
        }

        String ftpaRespondentOutOfTimeExplanation = asylumCase.read(FTPA_RESPONDENT_OUT_OF_TIME_EXPLANATION, String.class).orElse("");
        if (!ftpaRespondentOutOfTimeExplanation.isEmpty()) {
            asylumCase.write(IS_FTPA_RESPONDENT_OOT_EXPLANATION_VISIBLE_IN_SUBMITTED, YesOrNo.YES);
            asylumCase.write(IS_FTPA_RESPONDENT_OOT_EXPLANATION_VISIBLE_IN_DECIDED, YesOrNo.NO);
        }

        asylumCase.write(IS_FTPA_RESPONDENT_DOCS_VISIBLE_IN_SUBMITTED, YES);
        asylumCase.write(IS_FTPA_RESPONDENT_DOCS_VISIBLE_IN_DECIDED, NO);

        asylumCase.write(FTPA_RESPONDENT_DOCUMENTS, allRespondentDocuments);

        asylumCase.write(FTPA_RESPONDENT_APPLICATION_DATE, dateProvider.now().toString());

        asylumCase.write(FTPA_RESPONDENT_SUBMITTED, YES);
        asylumCase.write(APPEAL_DECISION_AVAILABLE, YesOrNo.YES);

        boolean isDlrmSetAside = featureToggler.getValue("dlrm-setaside-feature-flag", false);

        if (isDlrmSetAside) {
            final FtpaApplications newFtpaApplication =
                    FtpaApplications.builder()
                            .ftpaApplicant(RESPONDENT.toString())
                            .ftpaApplicationDate(dateProvider.now().toString())
                            .ftpaGroundsDocuments(ftpaRespondentGrounds)
                            .ftpaEvidenceDocuments(ftpaRespondentEvidence)
                            .build();

            maybeOutOfTimeDocuments.ifPresent(newFtpaApplication::setFtpaOutOfTimeDocuments);

            if (!ftpaRespondentOutOfTimeExplanation.isEmpty()) {
                newFtpaApplication.setFtpaOutOfTimeExplanation(ftpaRespondentOutOfTimeExplanation);
            }

            Optional<List<IdValue<FtpaApplications>>> maybeExistingFtpaApplictions =
                    asylumCase.read(FTPA_LIST);
            List<IdValue<FtpaApplications>> allApplications =
                    ftpaAppender.append(newFtpaApplication, maybeExistingFtpaApplictions.orElse(emptyList()));

            asylumCase.write(FTPA_LIST, allApplications);
            //asylumCase.write(IS_FTPA_LIST_VISIBLE, YesOrNo.YES);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
        
    }
}
