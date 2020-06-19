package uk.gov.hmcts.reform.iacaseapi.infrastructure.workallocation;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class TaskLog {
    private final Map<String, Long> log;
    private final List<Long> results;

    public TaskLog() {
        log = new HashMap<>();
        results = new ArrayList<>();
    }

    public void record(String id) {
        log.put(id, System.nanoTime());
    }

    public void end(String id) {
        Long now = System.nanoTime();
        Long startTime = log.remove(id);

        long totalMillis = (now - startTime) / 1000000;

        results.add(totalMillis);

        System.out.println("**************************************** Took [" + totalMillis + "] millis");
    }

    public String getFullResults() {
        return getAverageResults() + "\n\n" +results.stream().map(Object::toString).collect(Collectors.joining("\n"));
    }

    public String getAverageResults() {
        long averageTime = results.stream().mapToLong(results -> results).sum() / results.size();


        Map<Integer, Integer> counts = new TreeMap<>();


        results.forEach(results -> {
            int timeInSeconds = (int)(results / 1000);
            if (!counts.containsKey(timeInSeconds)) {
                counts.put(timeInSeconds, 1);
            } else {
                counts.put(timeInSeconds, counts.get(timeInSeconds) + 1);
            }
        });




        return "Count: " + results.size() + "\nAverage: " + averageTime + "\n\n" + counts;
    }
}
