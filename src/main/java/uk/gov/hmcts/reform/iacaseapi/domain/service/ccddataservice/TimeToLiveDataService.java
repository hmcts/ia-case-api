package uk.gov.hmcts.reform.iacaseapi.domain.service.ccddataservice;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.TTL;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdDataApi;

@Service
@Slf4j
public class TimeToLiveDataService extends CcdDataService {

    private FeatureToggler featureToggler;

    public TimeToLiveDataService(FeatureToggler featureToggler, CcdDataApi ccdDataApi, IdamService idamService, AuthTokenGenerator serviceAuthorization) {
        super(ccdDataApi, idamService, serviceAuthorization);
        this.featureToggler = featureToggler;
    }

    public void updateTheClock(Callback<AsylumCase> callback, boolean isToBeSuspended) {

        if (featureToggler.getValue("ia-retain-dispose", false)) {

            String caseId = String.valueOf(callback.getCaseDetails().getId());
            authorize(Event.MANAGE_CASE_TTL, caseId);

            StartEventDetails startEventDetails = startEvent(
                    userToken,
                    s2sToken,
                    uid,
                    JURISDICTION,
                    CASE_TYPE,
                    caseId,
                    Event.MANAGE_CASE_TTL);

            // Update TTL
            TTL ttlToBeUpdated = updateTTL(startEventDetails, isToBeSuspended);

            Map<String, Object> caseData = new HashMap<>();
            caseData.put(AsylumCaseFieldDefinition.TTL.value(), ttlToBeUpdated);

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("id", Event.MANAGE_CASE_TTL.toString());

            submitEvent(userToken, s2sToken, caseId, caseData, eventData,
                    startEventDetails.getToken(), true);

            log.info("TTL updated with systemTTL: {}, overrideTTL: {}, suspended: {}",
                    ttlToBeUpdated.getSystemTTL(),
                    ttlToBeUpdated.getOverrideTTL(),
                    ttlToBeUpdated.getSuspended());
        }

    }

    private TTL updateTTL(StartEventDetails startEventDetails, boolean isToBeSuspended) {

        TTL ttl = startEventDetails.getCaseDetails().getCaseData()
            .read(AsylumCaseFieldDefinition.TTL, TTL.class)
            .orElseThrow(() -> new IllegalStateException("TTL not present"));

        if (isToBeSuspended) {
            ttl.setSuspended(YesOrNo.YES);
        } else {
            ttl.setSuspended(YesOrNo.NO);
        }

        return ttl;
    }
}
