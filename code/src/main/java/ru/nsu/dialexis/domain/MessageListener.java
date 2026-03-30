package ru.nsu.dialexis.domain;

@FunctionalInterface
public interface MessageListener {
    void onMessage(ChatMessage message);
}
