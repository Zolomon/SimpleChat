package com.zolomon.simplechat.server.commands;

public interface Command {
    boolean isMatch(String line);
    void execute();
}
