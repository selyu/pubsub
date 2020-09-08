package org.selyu.pubsub;

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import com.rabbitmq.client.Connection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.selyu.pubsub.model.IMessage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RabbitMQPubSubTest {
    private Connection connection;
    private RabbitMQPubSub pubSub;

    @Before
    public void setUp() throws Exception {
        connection = new MockConnectionFactory().newConnection();
        pubSub = new RabbitMQPubSub(connection);
    }

    @Test
    public void test() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        pubSub.subscribe(StringMessage.class, __ -> latch.countDown());
        pubSub.publish(new StringMessage("Hello world!"));
        latch.await(1, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    public class StringMessage implements IMessage {
        private final String m;

        public StringMessage(String m) {
            this.m = m;
        }

        @Override
        public String toString() {
            return m;
        }
    }
}