package insertest.pipeline;

import insertest.FileProcessor;
import insertest.WriteQueue;
import insertest.structure.ConnectOpts;
import insertest.structure.StoredUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class Pipeline implements WriteQueue { // ex-ReactiveStorageBuffer
    private final static Logger log = LogManager.getLogger();

    // parallelism = 6 - I have 6 types/tables which can be written in parallel,
    // and 4 virtual CPUs, which should cope OK with that load
    private final Scheduler scheduler = Schedulers.newParallel("dbSink", 6, true /* daemon */);
    private final Writer dbWriter;

    private final Sinks.Many<StoredUnit> sink;

    private final BlockingQueue<StoredUnit> dbWriteQueue = new LinkedBlockingQueue<>(16_000);

    private final int BATCH_SIZE = 2;

    private final AtomicBoolean closed = new AtomicBoolean();

    public Pipeline(ConnectOpts dbConnectOptions, int jdbcPoolMax) {
        AtomicInteger currId = new AtomicInteger();
        fillBuffer(currId);
        ExecutorService executor = myExecutorService();
        this.dbWriter = new Writer(dbConnectOptions.asJDBCConnectOptions(), jdbcPoolMax);
        sink = Sinks.many().unicast().onBackpressureBuffer(dbWriteQueue);
        //sink.asFlux().subscribe(su -> counter.getAndIncrement());
        log.info("got {} StoredUnit(s)", dbWriteQueue.size());
        sink.asFlux()
            .groupBy(StoredUnit::getTableName)
            .flatMap(group ->  group.buffer(3).flatMapSequential(dbWriter::insert)
                            //.subscribe(/*it -> {log.info("XoXoXoXoXoXoXoXoXoXo", group.key());}*/)
//                    group.buffer(BATCH_SIZE)
//                    .publish().map(it -> {log.info("XoXoXoXoXoXoXoXoXoXo");
//                        var deferredInsertMono = Mono.defer(() -> Mono.fromCompletionStage(dbWriter.insert(it)));
//                        return Flux.concatDelayError(deferredInsertMono).then();
//                    })
            )
            .subscribe(result -> {}, error -> log.error("Pipeline error", error));
    }

    private void fillBuffer(AtomicInteger startId) {
        List<String> filesToProcess = Arrays.asList("file01", "file02");
        new Thread(new FileProcessor(filesToProcess, dbWriteQueue, startId)).start();
    }

    private ExecutorService myExecutorService() {
        // TODO nameFormat("db-writer-executor") etc.
        //  .setDaemon(true)
        //  .setUncaughtExceptionHandler(uncaughtExceptionHandler)
        return Executors.newSingleThreadExecutor();
    }

    public void put(StoredUnit su) {
        if (closed.get()) {
            throw new IllegalStateException("Pipeline is closed");
        }
        doEmit(() -> sink.tryEmitNext(su));
        log.info("put {} - {}", su.getClass().getSimpleName(), su.getNote());
    }

    private void doEmit(Supplier<EmitResult> supplier) {
        for (;;) {
            EmitResult emitResult = supplier.get();
            if (emitResult.isFailure()) {
                if (emitResult == Sinks.EmitResult.FAIL_OVERFLOW) {
                    continue;
                }
                emitResult.orThrow();
            }
            break;
        }
    }

    @Override
    public CompletableFuture<Void> flush() { // TODO
        return null;
    }

    @Override
    public CompletableFuture<Void> close() { // TODO
        return null;
    }
}
