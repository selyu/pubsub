package org.selyu.pubsub;

import org.selyu.pubsub.model.IMessage;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

public final class JedisPubSub extends PubSub {
    private final Pool<Jedis> jedisPool;
    private final String password;

    public JedisPubSub(Pool<Jedis> jedisPool, String password) throws InterruptedException {
        requireNonNull(jedisPool);
        this.jedisPool = jedisPool;
        this.password = password;

        CountDownLatch latch = new CountDownLatch(1);
        ForkJoinPool.commonPool().execute(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                if (password != null) {
                    jedis.auth(password);
                }

                jedis.subscribe(new redis.clients.jedis.JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        IMessage message1 = deserialize(message);
                        if (message1 != null) {
                            postToSubscribers(message1);
                        }
                    }

                    @Override
                    public void onSubscribe(String channel, int subscribedChannels) {
                        latch.countDown();
                    }
                }, "selyu:pubsub");
            }
        });

        boolean result = latch.await(5, TimeUnit.SECONDS);
        if (!result) {
            throw new RuntimeException("Took longer than 5 seconds to unlock.");
        }
    }

    @Override
    void sendData(String data) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (password != null) {
                jedis.auth(password);
            }
            jedis.publish("selyu:pubsub", data);
        }
    }
}
