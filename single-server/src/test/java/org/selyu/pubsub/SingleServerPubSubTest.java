package org.selyu.pubsub;

import org.junit.Test;

public class SingleServerPubSubTest {
    @Test
    public void test() throws InterruptedException {
        IPubSub pubSub = new SingleServerPubSub();
        pubSub.subscribe(HelloMessage.class, message -> System.out.println("Hello, " + message.getMessage()));
        pubSub.publish(new HelloMessage("World!")).join();

        Thread.sleep(100);
    }
}