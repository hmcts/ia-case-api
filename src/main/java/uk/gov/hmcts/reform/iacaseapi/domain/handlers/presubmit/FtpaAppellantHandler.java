package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties.APPELLANT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FtpaApplications;
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
public class FtpaAppellantHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;
    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;
    private final Appender<FtpaApplications> ftpaAppender;
    private final FeatureToggler featureToggler;

    public FtpaAppellantHandler(
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
               && callback.getEvent() == Event.APPLY_FOR_FTPA_APPELLANT;
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

        final Optional<List<IdValue<DocumentWithDescription>>>  maybeOutOfTimeDocuments = asylumCase.read(FTPA_APPELLANT_OUT_OF_TIME_DOCUMENTS);

        final List<IdValue<DocumentWithDescription>> ftpaAppellantOutOfTimeDocuments = maybeOutOfTimeDocuments.orElse(Collections.emptyList());

        final Optional<List<IdValue<DocumentWithDescription>>>  maybeDocument = asylumCase.read(FTPA_APPELLANT_GROUNDS_DOCUMENTS);

        final List<IdValue<DocumentWithDescription>> ftpaAppellantGrounds = maybeDocument.orElse(Collections.emptyList());

        final Optional<List<IdValue<DocumentWithDescription>>> maybeFtpaAppellantEvidence = asylumCase.read(FTPA_APPELLANT_EVIDENCE_DOCUMENTS);

        final List<IdValue<DocumentWithDescription>> ftpaAppellantEvidence = maybeFtpaAppellantEvidence.orElse(Collections.emptyList());

        final Optional<List<IdValue<DocumentWithMetadata>>> maybeFtpaAppellantDocuments = asylumCase.read(FTPA_APPELLANT_DOCUMENTS);

        final List<IdValue<DocumentWithMetadata>> existingFtpaAppellantDocuments = maybeFtpaAppellantDocuments.orElse(Collections.emptyList());

        final List<DocumentWithMetadata> ftpaAppellantDocuments = new ArrayList<>();

        ftpaAppellantDocuments.addAll(
            documentReceiver
                .tryReceiveAll(
                    ftpaAppellantOutOfTimeDocuments,
                    DocumentTag.FTPA_APPELLANT
                )
        );

        ftpaAppellantDocuments.addAll(
            documentReceiver
                .tryReceiveAll(
                    ftpaAppellantGrounds,
                    DocumentTag.FTPA_APPELLANT
                )
        );

        ftpaAppellantDocuments.addAll(
            documentReceiver
                .tryReceiveAll(
                    ftpaAppellantEvidence,
                    DocumentTag.FTPA_APPELLANT
                )
        );

        final List<IdValue<DocumentWithMetadata>> allAppellantDocuments =
            documentsAppender.append(
                existingFtpaAppellantDocuments,
                ftpaAppellantDocuments
            );

        final String currentDate = dateProvider.now().toString();

        if (maybeDocument.isPresent()) {
            asylumCase.write(IS_FTPA_APPELLANT_GROUNDS_DOCS_VISIBLE_IN_SUBMITTED, YES);
            asylumCase.write(IS_FTPA_APPELLANT_GROUNDS_DOCS_VISIBLE_IN_DECIDED, NO);
        }
        if (maybeFtpaAppellantEvidence.isPresent()) {
            asylumCase.write(IS_FTPA_APPELLANT_EVIDENCE_DOCS_VISIBLE_IN_SUBMITTED, YES);
            asylumCase.write(IS_FTPA_APPELLANT_EVIDENCE_DOCS_VISIBLE_IN_DECIDED, NO);
        }
        if (maybeOutOfTimeDocuments.isPresent()) {
            asylumCase.write(IS_FTPA_APPELLANT_OOT_DOCS_VISIBLE_IN_SUBMITTED, YES);
            asylumCase.write(IS_FTPA_APPELLANT_OOT_DOCS_VISIBLE_IN_DECIDED, NO);
        }

        String ftpaAppellantOutOfTimeExplanation = asylumCase.read(FTPA_APPELLANT_OUT_OF_TIME_EXPLANATION, String.class).orElse("");
        if (!ftpaAppellantOutOfTimeExplanation.isEmpty()) {
            asylumCase.write(IS_FTPA_APPELLANT_OOT_EXPLANATION_VISIBLE_IN_SUBMITTED, YesOrNo.YES);
            asylumCase.write(IS_FTPA_APPELLANT_OOT_EXPLANATION_VISIBLE_IN_DECIDED, YesOrNo.NO);
        }

        asylumCase.write(IS_FTPA_APPELLANT_DOCS_VISIBLE_IN_SUBMITTED, YES);
        asylumCase.write(IS_FTPA_APPELLANT_DOCS_VISIBLE_IN_DECIDED, NO);

        asylumCase.write(FTPA_APPELLANT_DOCUMENTS, allAppellantDocuments);

        asylumCase.write(FTPA_APPELLANT_APPLICATION_DATE, currentDate);

        asylumCase.write(FTPA_APPELLANT_SUBMITTED, YES);

        asylumCase.write(APPEAL_DECISION_AVAILABLE, YesOrNo.YES);

        String ftpaAppellantGroundsText = asylumCase.read(FTPA_APPELLANT_GROUNDS, String.class).orElse("");
        addToFtpaList(asylumCase, currentDate, ftpaAppellantGrounds, ftpaAppellantEvidence,
                maybeOutOfTimeDocuments, ftpaAppellantOutOfTimeExplanation, ftpaAppellantGroundsText);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void addToFtpaList(AsylumCase asylumCase, String currentDate,
                               List<IdValue<DocumentWithDescription>> ftpaAppellantGrounds,
                               List<IdValue<DocumentWithDescription>> ftpaAppellantEvidence,
                               Optional<List<IdValue<DocumentWithDescription>>> maybeOutOfTimeDocuments,
                               String ftpaAppellantOutOfTimeExplanation,
                               String ftpaAppellantGroundsText) {
        boolean isDlrmSetAside = featureToggler.getValue("dlrm-setaside-feature-flag", false);

        if (isDlrmSetAside) {
            final FtpaApplications newFtpaApplication =
                    FtpaApplications.builder()
                            .ftpaApplicant(APPELLANT.toString())
                            .ftpaApplicationDate(currentDate)
                            .ftpaAppellantGroundsText(ftpaAppellantGroundsText)
                            .ftpaGroundsDocuments(ftpaAppellantGrounds)
                            .ftpaEvidenceDocuments(ftpaAppellantEvidence)
                            .build();

            maybeOutOfTimeDocuments.ifPresent(newFtpaApplication::setFtpaOutOfTimeDocuments);

            if (!ftpaAppellantOutOfTimeExplanation.isEmpty()) {
                newFtpaApplication.setFtpaOutOfTimeExplanation(ftpaAppellantOutOfTimeExplanation);
            }

            Optional<List<IdValue<FtpaApplications>>> maybeExistingFtpaApplictions =
                    asylumCase.read(FTPA_LIST);
            List<IdValue<FtpaApplications>> allApplications =
                    ftpaAppender.append(newFtpaApplication, maybeExistingFtpaApplictions.orElse(emptyList()));

            asylumCase.write(FTPA_LIST, allApplications);
            asylumCase.write(IS_FTPA_LIST_VISIBLE, YesOrNo.YES);
        }
    }
}
