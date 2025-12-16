package uk.gov.hmcts.reform.iacaseapi.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;

import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.XUI_BANNER_TEXT;

@Slf4j
@Service
public class BannerTextService {

    public String getBannerText(AsylumCase asylumCase) {
        Optional<String> read = asylumCase.read(XUI_BANNER_TEXT);
        return read.orElse(EMPTY);
    }

    public void addBannerText(AsylumCase asylumCase, String bannerText) {
        if (StringUtils.hasText(bannerText)) {
            asylumCase.write(XUI_BANNER_TEXT, bannerText);
        } else {
            log.error("Banner text value is {}", bannerText);
            throw new IllegalArgumentException("Banner text can not be null or empty");
        }

    }

}
