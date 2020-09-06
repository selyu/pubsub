package org.selyu.pubsub;

import org.selyu.pubsub.model.IMessage;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface IPubSub {
    <T extends IMessage> void subscribe(Class<T> messageClass, Consumer<T> subscriber);

    CompletableFuture<Void> publish(IMessage message);
}
