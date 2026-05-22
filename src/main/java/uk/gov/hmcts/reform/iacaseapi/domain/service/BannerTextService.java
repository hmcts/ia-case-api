package uk.gov.hmcts.reform.iacaseapi.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;

import java.util.Optional;
import java.util.regex.Pattern;

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
        // Don't add the text if we already have it for some reason - check word boundaries to avoid accidental substring matches
        if (!existingIncludesNew(existingBannerText, bannerText)) {
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
        if (existingIncludesNew(existingBannerText, bannerText)) {
            String bannerTextAfterRemove = existingBannerText.replace(bannerText, EMPTY);
            addBannerText(asylumCase, bannerTextAfterRemove);
        }
    }

    private boolean existingIncludesNew(String existingBannerText, String bannerText) {
        // Need to encode ()s and other non-alphanumeric chars in order for \b to work
        String existing = existingBannerText.toLowerCase()
                          .replace("(", "LBRACKET")
                          .replace(")", "RBRACKET");
        String target = bannerText.toLowerCase()
                          .replace("(", "LBRACKET")
                          .replace(")", "RBRACKET");
        String regex = "\\b" + Pattern.quote(target) + "\\b";

        return Pattern.compile(regex).matcher(existing).find();
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
