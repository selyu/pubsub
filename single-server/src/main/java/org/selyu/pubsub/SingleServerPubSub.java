package org.selyu.pubsub;

import org.selyu.pubsub.model.IMessage;

public final class SingleServerPubSub extends PubSub {
    @Override
    void sendData(String data) {
        IMessage message = deserialize(data);
        if (message != null) {
            postToSubscribers(message);
        }
    }
}
