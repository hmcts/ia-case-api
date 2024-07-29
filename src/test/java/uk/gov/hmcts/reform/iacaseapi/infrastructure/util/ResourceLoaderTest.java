package uk.gov.hmcts.reform.iacaseapi.infrastructure.util;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.ResourceLoader.loadJson;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.ResourceLoader.loadResource;

import java.nio.charset.Charset;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.ResourceLoader;

@RunWith(MockitoJUnitRunner.class)
public class ResourceLoaderTest {

    private String filePath = "testFile.txt";

    @Test
    void shouldLoadJson() throws Exception {
        String expected = new String(loadResource(filePath), Charset.forName("utf-8"));

        assertEquals(expected, loadJson(filePath));
    }

    @Test
    void shouldLoadResource() throws Exception {
        byte[] expected = ResourceLoader.class.getClassLoader().getResourceAsStream(filePath).readAllBytes();

        assertArrayEquals(expected, loadResource(filePath));
    }

    @Test
    void shouldThrowExceptionForFileNotFound() throws Exception {
        String wrongFilePath = "wrongFilePath";
        String pathToEmptyFile = "emptyTestFile.txt";

        assertThrows(IllegalArgumentException.class,
            () -> loadResource(wrongFilePath));
    }
}
