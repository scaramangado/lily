package de.scaramanga.lily.core.communication;

public interface MessageInfo {

    static MessageInfo empty() {
        return new MessageInfo() {};
    }
}
