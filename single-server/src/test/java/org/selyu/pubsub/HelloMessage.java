package org.selyu.pubsub;

import org.selyu.pubsub.model.IMessage;

public final class HelloMessage implements IMessage {
    private final String message;

    public HelloMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
