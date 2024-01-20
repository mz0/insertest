package insertest;

import insertest.structure.StoredUnit;

import java.util.concurrent.CompletableFuture;

public interface WriteQueue {

    void put(StoredUnit row);

    CompletableFuture<Void> flush();

    CompletableFuture<Void> close();
}
