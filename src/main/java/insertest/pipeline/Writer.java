package insertest.pipeline;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Writer {
    private final static Logger log = LogManager.getLogger();

    public CompletionStage<Object> longOp(int remainder, List<Integer> sequence) {
        try {
            TimeUnit.MILLISECONDS.sleep(5 + (15 * remainder) + Math.round(5 * Math.random()));
            log.info("thread {} / rem{}: {}", Thread.currentThread().getName(), remainder, intSeq(sequence));
            return CompletableFuture.completedFuture(null);
        } catch (InterruptedException ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }
    private static String intSeq(List<Integer> seq) {
        return String.join(",", seq.stream().map(i -> i.toString()).collect(Collectors.toList()));
    }
}
