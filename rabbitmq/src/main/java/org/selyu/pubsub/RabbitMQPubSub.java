package org.selyu.pubsub;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.selyu.pubsub.model.IMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;

public final class RabbitMQPubSub extends PubSub {
    private final Channel channel;

    public RabbitMQPubSub(Connection connection) throws IOException {
        requireNonNull(connection);
        channel = connection.createChannel();

        channel.queueDeclare("selyu:pubsub", false, false, false, null);
        channel.basicConsume("selyu:pubsub", true, (__, delivery) -> {
            IMessage message = deserialize(new String(delivery.getBody(), StandardCharsets.UTF_8));
            if (message != null) {
                postToSubscribers(message);
            }
        }, __ -> {
        });
    }

    @Override
    void sendData(String data) {
        try {
            channel.basicPublish("", "selyu:pubsub", null, data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
