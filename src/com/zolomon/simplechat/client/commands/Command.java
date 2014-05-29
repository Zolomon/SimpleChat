package com.zolomon.simplechat.client.commands;

public interface Command {
    boolean isMatch(String line);
    void execute();
}
