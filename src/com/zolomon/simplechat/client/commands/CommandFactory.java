package com.zolomon.simplechat.client.commands;

import com.zolomon.simplechat.client.ConnectionThread;
import com.zolomon.simplechat.server.commands.BasicCommand;

import java.util.ArrayList;

public class CommandFactory {

    private CommandFactory() {

    }

    public static ArrayList<BasicCommand> getCommands(final ConnectionThread ct) {
        ArrayList<BasicCommand> commands = new ArrayList<BasicCommand>();

        commands.add(buildWhoCmd(ct));
        commands.add(buildQuitCmd(ct));
        commands.add(buildJoinChannelCmd(ct));
        //commands.add(buildListChannelsCmd(ct));
        //commands.add(buildGetChannelTopicCmd(ct));
        //commands.add(buildSetChannelTopicCmd(ct));
        //commands.add(buildIgnoreUserCmd(ct));
        //commands.add(buildPrivateMessageCmd(ct));
        //commands.add(buildCmd(ct));

        return commands;
    }

    private static BasicCommand buildJoinChannelCmd(final ConnectionThread ct) {
        return new BasicCommand("(?:/j|/join) #(.+)") {

            @Override
            public void execute() {

            }};
    }

    private static BasicCommand buildQuitCmd(final ConnectionThread ct) {
        return new BasicCommand("/quit") {

            @Override
            public void execute() {
                ct.dispatchMessage("Server", ct.getAuthor() + " has disconnected.");
            }

            @Override
            public boolean terminate() {
                return true;
            }

        };
    }

    private static BasicCommand buildWhoCmd(final ConnectionThread ct) {
        return new BasicCommand("/who") {

            @Override
            public void execute() {
                ct.write("Connected users: ");
                ct.getOnShowUsersCallback().execute();
            }

        };
    }
}
