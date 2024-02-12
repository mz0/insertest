package insertest;

import insertest.pipeline.Writer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class App2 {
    public static void main(String[] args) {
        Logger log = LogManager.getLogger();
        final int modulo = 11;
        var writer = new Writer();
        Flux.range(0, 160).groupBy(i -> i % modulo)
                .flatMap(groupedFlux -> groupedFlux.buffer(3)
                      //.publishOn(Schedulers.newParallel("async", modulo)) // FIXME main() will not finish!
                        // daemon=true - main() does no work, =false - hangs as above
                      //.publishOn(Schedulers.newParallel("par", modulo, /* daemon */ false))
                        .flatMap(list -> {
                            log.info("{} mod{}", groupedFlux.key(), modulo);
                            Mono<Object> asyncOpResult = Mono.defer(() -> Mono.fromCompletionStage(
                                    writer.longOp(groupedFlux.key(), list))
                            );
                            return Flux.concatDelayError(asyncOpResult);
                        })
                )
                .subscribe(r -> log.info("{}", r), error -> log.error("{}", error));
    }
}
