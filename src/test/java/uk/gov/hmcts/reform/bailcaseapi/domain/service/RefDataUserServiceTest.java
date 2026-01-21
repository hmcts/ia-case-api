package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.refdata.CommonDataRefApi;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RefDataUserServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CommonDataRefApi commonDataRefApi;

    @Mock
    private UserDetails userDetails;

    @Mock
    private CommonDataResponse commonDataResponse;

    @Mock
    private CategoryValues categoryValues;

    private RefDataUserService refDataUserService;

    private final String categoryId = "category";

    @BeforeEach
    void setup() {
        refDataUserService = new RefDataUserService(
            authTokenGenerator,
            commonDataRefApi,
            userDetails
        );
    }

    @Test
    void shouldRetrieveCategoryValues() {
        String token = "token";
        when(userDetails.getAccessToken()).thenReturn(token);
        String authToken = "authToken";
        when(authTokenGenerator.generate()).thenReturn(authToken);
        String serviceId = "BFA1";
        when(commonDataRefApi.getAllCategoryValuesByCategoryId(
            token,
            authToken,
            categoryId,
            serviceId,
            "Y"
        )).thenReturn(commonDataResponse);

        assertEquals(commonDataResponse,
                     refDataUserService.retrieveCategoryValues(categoryId, "Y"));
    }

    @Test
    void shouldHandleExceptionRetrieveCategoryValues() {
        // mock the exception and make sure it is handled
        String token = "token";
        when(userDetails.getAccessToken()).thenReturn(token);
        String authToken = "authToken";
        when(authTokenGenerator.generate()).thenReturn(authToken);
        String serviceId = "BFA1";
        when(commonDataRefApi.getAllCategoryValuesByCategoryId(
            token,
            authToken,
            categoryId,
            serviceId,
            "Y"
        )).thenThrow(new RuntimeException("Gateway timeout"));

        commonDataResponse = assertDoesNotThrow(() -> refDataUserService.retrieveCategoryValues(categoryId, "Y"));
        assertNull(commonDataResponse);
    }

    @Test
    void shouldFilterCategoryValuesByCategoryId() {
        List<CategoryValues> listOfCategoryValues = List.of(categoryValues);
        when(commonDataResponse.getCategoryValues()).thenReturn(listOfCategoryValues);
        when(categoryValues.getCategoryKey()).thenReturn(categoryId);

        assertEquals(listOfCategoryValues,
                     refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, categoryId));
    }

    @Test
    void shouldFilterCategoryValuesByCategoryIdWithActiveFlag() {
        List<CategoryValues> listOfCategoryValues = List.of(categoryValues);
        when(commonDataResponse.getCategoryValues()).thenReturn(listOfCategoryValues);
        when(categoryValues.getCategoryKey()).thenReturn(categoryId);
        when(categoryValues.getActiveFlag()).thenReturn("Y");

        assertEquals(listOfCategoryValues,
                     refDataUserService.filterCategoryValuesByCategoryIdWithActiveFlag(commonDataResponse, categoryId));
    }

    @Test
    void shouldMapCategoryValuesToDynamicListValues() {
        List<CategoryValues> listOfCategoryValues = List.of(categoryValues);
        when(categoryValues.getKey()).thenReturn("key");
        when(categoryValues.getValueEn()).thenReturn("valueEn");

        List<Value> listOfValues = refDataUserService
            .mapCategoryValuesToDynamicListValues(listOfCategoryValues);

        assertEquals("key", listOfValues.get(0).getCode());
        assertEquals("valueEn", listOfValues.get(0).getLabel());
    }
}

