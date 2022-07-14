package uk.gov.hmcts.reform.bailcaseapi.util;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class MapFieldAssertor {

    private MapFieldAssertor() {
        // noop
    }

    public static void assertFields(
        Map<String, Object> expectedMap,
        Map<String, Object> actualMap
    ) {
        assertFields(expectedMap, actualMap, "");
    }

    public static void assertFields(
        Map<String, Object> expectedMap,
        Map<String, Object> actualMap,
        final String path
    ) {
        for (Map.Entry<String, Object> expectedEntry : expectedMap.entrySet()) {

            String key = expectedEntry.getKey();
            String pathWithKey = path + "." + key;

            Object expectedValue = expectedEntry.getValue();
            Object actualValue = actualMap.get(key);

            if ((expectedValue instanceof List) && (actualValue instanceof List)) {

                List expectedValueCollection = (List) expectedValue;
                List actualValueCollection = (List) actualValue;

                for (int i = 0; i < expectedValueCollection.size(); i++) {

                    String pathWithKeyAndIndex = pathWithKey + "." + i;

                    Object expectedValueItem = expectedValueCollection.get(i);
                    Object actualValueItem =
                        i < actualValueCollection.size()
                            ? actualValueCollection.get(i)
                            : null;

                    assertValue(expectedValueItem, actualValueItem, pathWithKeyAndIndex);
                }

            } else {
                assertValue(expectedValue, actualValue, pathWithKey);
            }
        }
    }

    private static void assertValue(
        Object expectedValue,
        Object actualValue,
        String path
    ) {
        if ((expectedValue instanceof Map) && (actualValue instanceof Map)) {

            assertFields(
                (Map<String, Object>) expectedValue,
                (Map<String, Object>) actualValue,
                path
            );

        } else {

            if ((expectedValue instanceof String) && (actualValue instanceof String)) {

                String expectedValueString = (String) expectedValue;

                if (expectedValueString.length() > 3
                    && expectedValueString.startsWith("$/")
                    && expectedValueString.endsWith("/")) {

                    expectedValueString = expectedValueString.substring(2, expectedValueString.length() - 1);

                    String actualValueString = (String) actualValue;

                    assertThat(
                        "Expected field matches regular expression (" + path + ")",
                        actualValueString,
                        matchesPattern(expectedValueString)
                    );

                    return;
                }

                if (expectedValueString.startsWith("contains(")) {
                    assertThat(
                        "Expected field contains (" + path + ")",
                        String.valueOf(actualValue),
                        // extract value from contains() wrapper
                        containsString(expectedValueString.substring(9, expectedValueString.length() - 1))
                    );

                    return;
                }
            }

            assertThat(
                "Expected field matches (" + path + ")",
                actualValue,
                equalTo(expectedValue)
            );
        }
    }
}
