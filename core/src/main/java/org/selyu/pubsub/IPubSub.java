package org.selyu.pubsub;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface IPubSub {
    <T> void subscribe(Class<T> messageClass, Consumer<T> subscriber);

    CompletableFuture<Void> publish(Object message);
}
