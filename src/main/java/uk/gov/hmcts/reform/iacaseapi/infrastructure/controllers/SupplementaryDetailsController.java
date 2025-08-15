package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.status;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SupplementaryInfo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.SupplementaryDetailsService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.MissingSupplementaryInfo;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.SupplementaryDetailsRequest;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.SupplementaryDetailsResponse;

@Slf4j
@RestController
public class SupplementaryDetailsController {

    private final SupplementaryDetailsService supplementaryDetailsService;

    public SupplementaryDetailsController(SupplementaryDetailsService supplementaryDetailsService) {
        this.supplementaryDetailsService = supplementaryDetailsService;
    }

    @Operation(
        summary = "Handles 'supplementary-details' calls from Pay Hub",
        security =
            {
                @SecurityRequirement(name = "Authorization"),
                @SecurityRequirement(name = "ServiceAuthorization")
            }
    )
    @ApiResponses({
        @ApiResponse(
            responseCode  = "200",
            description = "Supplementary details completely retrieved.",
            content =  @Content(schema = @Schema(implementation = String.class))
            ),
        @ApiResponse(
            responseCode = "206",
            description = "Supplementary details partially retrieved.",
            content =  @Content(schema = @Schema(implementation = String.class))
            ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - missing or invalid S2S token."
            ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - system user does not have access to the resources."
            ),
        @ApiResponse(
            responseCode = "404",
            description = "Supplementary details not found for all the case numbers given."
            ),
        @ApiResponse(
            responseCode = "500",
            description = "Unexpected or Run time exception."
            )
    })
    @PostMapping(path = "/supplementary-details")
    public ResponseEntity<SupplementaryDetailsResponse> post(
        @RequestBody SupplementaryDetailsRequest supplementaryDetailsRequest) {

        if (supplementaryDetailsRequest == null
            || supplementaryDetailsRequest.getCcdCaseNumbers() == null) {
            return badRequest().build();
        }

        List<String> ccdCaseNumberList = supplementaryDetailsRequest
            .getCcdCaseNumbers()
            .stream()
            .distinct()
            .collect(Collectors.toList());

        if (ccdCaseNumberList.isEmpty()) {

            log.info("Request ccdNumberList: is empty list");

            return status(HttpStatus.OK).body(
                new SupplementaryDetailsResponse(
                    Collections.emptyList(),
                    missingSupplementaryDetailsInfo(
                        ccdCaseNumberList,
                        Collections.emptyList()
                    )
                )
            );
        }

        log.info("Request ccdNumberList:"
                 + String.join(",", ccdCaseNumberList));

        try {

            SupplementaryDetailsResponse supplementaryDetailsResponse = null;

            List<SupplementaryInfo> supplementaryInfo = supplementaryDetailsService
                .getSupplementaryDetails(ccdCaseNumberList);

            if (supplementaryInfo == null) {
                return status(HttpStatus.FORBIDDEN).body(supplementaryDetailsResponse);
            }

            supplementaryDetailsResponse = new SupplementaryDetailsResponse(
                supplementaryInfo, missingSupplementaryDetailsInfo(ccdCaseNumberList, supplementaryInfo));

            if (supplementaryDetailsResponse.getSupplementaryInfo().isEmpty()) {
                return status(HttpStatus.NOT_FOUND).body(supplementaryDetailsResponse);

            } else if (supplementaryDetailsResponse.getSupplementaryInfo().size() < ccdCaseNumberList.size()) {
                return status(HttpStatus.PARTIAL_CONTENT).body(supplementaryDetailsResponse);

            } else if (supplementaryDetailsResponse.getSupplementaryInfo().size() == ccdCaseNumberList.size()) {
                return status(HttpStatus.OK).body(supplementaryDetailsResponse);

            } else {
                return status(HttpStatus.INTERNAL_SERVER_ERROR).body(supplementaryDetailsResponse);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
            return status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private MissingSupplementaryInfo missingSupplementaryDetailsInfo(
        List<String> ccdCaseNumberList,
        List<SupplementaryInfo> supplementaryInfo) {

        List<String> ccdCaseNumbersFound = supplementaryInfo
            .stream()
            .map(SupplementaryInfo::getCcdCaseNumber)
            .toList();

        List<String> ccdCaseNumbersMissing = ccdCaseNumberList
            .stream()
            .filter(ccdNumber -> !ccdCaseNumbersFound.contains(ccdNumber))
            .collect(Collectors.toList());

        return ccdCaseNumbersMissing.isEmpty() ? null : new MissingSupplementaryInfo(ccdCaseNumbersMissing);
    }
}
