package org.selyu.pubsub;

import com.google.gson.Gson;

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
    private final Map<Class<?>, Set<Consumer<Object>>> messageToSubscriberMap = new HashMap<>();
    private final Gson gson = new Gson();

    <T> T deserialize(String data) {
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

    void postToSubscribers(Object message) {
        for (Consumer<Object> consumer : messageToSubscriberMap.getOrDefault(message.getClass(), new HashSet<>())) {
            consumer.accept(message);
        }
    }

    abstract void sendData(String data);

    @Override
    public <T> void subscribe(Class<T> messageClass, Consumer<T> subscriber) {
        messageToSubscriberMap.compute(messageClass, (aClass, consumers) -> {
            if (consumers == null)
                consumers = new HashSet<>();
            //noinspection unchecked
            consumers.add((Consumer<Object>) subscriber);
            return consumers;
        });
    }

    @Override
    public CompletableFuture<Void> publish(Object message) {
        requireNonNull(message);
        return CompletableFuture.runAsync(() -> {
            String json = URLEncoder.encode(gson.toJson(message), StandardCharsets.UTF_8);
            String data = String.format("%s@%s", message.getClass().getName(), json);
            sendData(data);
        }, QUEUE_EXECUTOR);
    }
}
