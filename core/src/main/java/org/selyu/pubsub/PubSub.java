package org.selyu.pubsub;

import com.google.gson.Gson;
import org.selyu.pubsub.model.IMessage;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

abstract class PubSub implements IPubSub {
    private static final Executor QUEUE_EXECUTOR = Executors.newFixedThreadPool(5);
    private final Map<Class<? extends IMessage>, Set<Consumer<IMessage>>> messageToSubscriberMap = new HashMap<>();
    private final Gson gson = new Gson();

    <T extends IMessage> T deserialize(String data) {
        String[] split = data.split("@", 2);
        if (split.length < 2) {
            return null;
        }

        Class<T> type;
        try {
            //noinspection unchecked
            type = (Class<T>) Class.forName(split[0]);
        } catch (ClassNotFoundException | ClassCastException e) {
            e.printStackTrace();
            return null;
        }

        String json = URLDecoder.decode(split[1], StandardCharsets.UTF_8);
        return gson.fromJson(json, type);
    }

    void postToSubscribers(IMessage message) {
        for (Consumer<IMessage> consumer : messageToSubscriberMap.getOrDefault(message.getClass(), new HashSet<>())) {
            consumer.accept(message);
        }
    }

    abstract void sendData(String data);

    @Override
    public <T extends IMessage> void subscribe(Class<T> messageClass, Consumer<T> subscriber) {
        messageToSubscriberMap.compute(messageClass, (aClass, consumers) -> {
            if (consumers == null)
                consumers = new HashSet<>();
            consumers.add((Consumer<IMessage>) subscriber);
            return consumers;
        });
    }

    @Override
    public CompletableFuture<Void> publish(IMessage message) {
        requireNonNull(message);
        return CompletableFuture.runAsync(() -> {
            String json = URLEncoder.encode(gson.toJson(message), StandardCharsets.UTF_8);
            String data = String.format("%s@%s", message.getClass().getName(), json);
            sendData(data);
        }, QUEUE_EXECUTOR);
    }
}
