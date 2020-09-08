package org.selyu.pubsub;

public final class SingleServerPubSub extends PubSub {
    @Override
    void sendData(String data) {
        Object message = deserialize(data);
        if (message != null) {
            postToSubscribers(message);
        }
    }
}
