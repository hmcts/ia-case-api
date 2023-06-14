package uk.gov.hmcts.reform.iacaseapi.domain.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CommonDataRefApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;


@Slf4j
@Service
public class RefDataUserService {

    @Autowired
    AuthTokenGenerator authTokenGenerator;

    @Autowired
    CommonDataRefApi commonDataRefApi;

    public static final String SERVICE_ID = "ABA5";

    private CommonDataResponse commonDataResponse;

    public CommonDataResponse retrieveCategoryValues(String authorization, String categoryId,String isHearingChildRequired) {
        log.info("retrieveCategoryValues {}", categoryId);
        try {
            commonDataResponse = commonDataRefApi.getAllCategoryValuesByCategoryId(
                authorization,
                authTokenGenerator.generate(),
                categoryId,
                SERVICE_ID,
                isHearingChildRequired
            );

        } catch (Exception e) {
            log.error("Category Values look up failed {} ", e.getMessage());
        }
        return commonDataResponse;
    }

}



