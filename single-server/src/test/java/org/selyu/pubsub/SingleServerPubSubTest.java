package org.selyu.pubsub;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SingleServerPubSubTest {
    @Test
    public void test() throws InterruptedException {
        IPubSub pubSub = new SingleServerPubSub();
        CountDownLatch latch = new CountDownLatch(1);
        pubSub.subscribe(String.class, __ -> latch.countDown());
        pubSub.publish("Hello World").join();

        latch.await(1, TimeUnit.SECONDS);
    }
}