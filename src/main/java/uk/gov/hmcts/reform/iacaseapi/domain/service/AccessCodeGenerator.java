package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.security.SecureRandom;
import org.apache.commons.lang3.RandomStringUtils;

public class AccessCodeGenerator {

    private static final String ALLOWED_CHARS = "ABCDEFGHJKLMNPRSTVWXYZ23456789";
    private static final int LENGTH = 12;

    private AccessCodeGenerator() {
    }

    public static String generateAccessCode() {
        return RandomStringUtils.random(LENGTH, 0, ALLOWED_CHARS.length(),
                false, false, ALLOWED_CHARS.toCharArray(), new SecureRandom());
    }
}
