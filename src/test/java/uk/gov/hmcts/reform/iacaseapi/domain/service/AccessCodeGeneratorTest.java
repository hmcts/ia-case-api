package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;


@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class AccessCodeGeneratorTest {

    private static final int TEST_CYCLES = 10;
    private static final String ACCESS_CODE_ALLOWED_CHARS = "ABCDEFGHJKLMNPRSTVWXYZ23456789";
    private static final int ACCESS_CODE_LENGTH = 12;

    @Test
    public void accessCodes_have_correct_length_and_characters() {
        for (int index = 0; index < TEST_CYCLES; index++) {
            String accessCode = AccessCodeGenerator.generateAccessCode();
            assertEquals(ACCESS_CODE_LENGTH, accessCode.length());
            assertEquals(ACCESS_CODE_LENGTH, accessCode.chars().filter(i -> ACCESS_CODE_ALLOWED_CHARS.contains(Character.toString(i))).count());
        }
    }

    @Test
    public void accessCodes_should_be_different() {
        Set<String> accessCodes = new HashSet<>();

        for (int index = 0; index < TEST_CYCLES; index++) {
            accessCodes.add(AccessCodeGenerator.generateAccessCode());
        }

        assertEquals(TEST_CYCLES, accessCodes.size());
    }
}
