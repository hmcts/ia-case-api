package uk.gov.hmcts.reform.iacaseapi.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;

import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.springframework.util.StringUtils.hasText;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.XUI_BANNER_TEXT;

@Slf4j
@Service
public class BannerTextService {

    public void addToBannerText(AsylumCase asylumCase, String bannerText) {
        validateText(bannerText);
        String existingBannerText = getBannerText(asylumCase);
        if (!existingBannerText.equalsIgnoreCase(bannerText)) {
            StringBuilder existingTextBuilder = new StringBuilder(existingBannerText);
            StringBuilder newBannerText;
            if (hasText(existingTextBuilder)) {
                newBannerText = existingTextBuilder.append(SPACE).append(bannerText);
            } else {
                newBannerText = existingTextBuilder.append(bannerText);
            }
            addBannerText(asylumCase, newBannerText.toString());
        }
    }

    public void removeFromBannerText(AsylumCase asylumCase, String bannerText) {
        validateText(bannerText);
        String existingBannerText = getBannerText(asylumCase);
        if (existingBannerText.toLowerCase().contains(bannerText.toLowerCase())) {
            String bannerTextAfterRemove = existingBannerText.replace(bannerText, EMPTY);
            addBannerText(asylumCase, bannerTextAfterRemove);
        }
    }

    private String getBannerText(AsylumCase asylumCase) {
        Optional<String> read = asylumCase.read(XUI_BANNER_TEXT);
        return read.orElse(EMPTY);
    }

    private void addBannerText(AsylumCase asylumCase, String bannerText) {
        asylumCase.write(XUI_BANNER_TEXT, bannerText.trim());
    }

    private void validateText(String text) {
        if (!hasText(text)) {
            log.error("Banner text can not be null or empty");
            throw new IllegalArgumentException("Banner text can not be null or empty");
        }
    }


}
