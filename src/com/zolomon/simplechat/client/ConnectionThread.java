
package com.zolomon.simplechat.client;

import com.zolomon.simplechat.client.commands.CommandFactory;
import com.zolomon.simplechat.server.commands.BasicCommand;
import com.zolomon.simplechat.server.commands.Command;
import com.zolomon.simplechat.shared.Callback;
import com.zolomon.simplechat.shared.Message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ConnectionThread implements Runnable {
    private Socket socket;

    private BufferedReader reader;

    private PrintWriter writer;

    private PrintWriter serverStream;

    private BlockingQueue<Message> queue;

    private String author;

    private Thread thread;

    private Callback onShowUsersCallback;

    private ArrayList<BasicCommand> commands;

    protected boolean isQuitting;

    private Callback onQuit;

    private Callback onQueryName;

    public ConnectionThread(Socket socket, BufferedWriter serverStream, BlockingQueue<Message> queue)
            throws IOException {
        this.socket = socket;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream())));
        this.serverStream = new PrintWriter(serverStream);
        this.queue = queue;
        this.commands = new ArrayList<BasicCommand>();

        addCommands();
    }

    private void addCommands() {
        commands = CommandFactory.getCommands(this);
    }

    @Override
    public void run() {
        String msg = null;
        try {
            onQueryName.execute();

            dispatchMessage("Server", author + " has connected.");

            showUsers();

            outerloop: while ((msg = reader.readLine()) != null) {
                System.out.println("'" + msg + "'");

                for (BasicCommand cmd : commands) {
                    if (cmd.isMatch(msg)) {
                        cmd.execute();
                        if (cmd.terminate()) {
                            System.out.println("Terminating " + author);
                            break outerloop;
                        }
                    }
                }

                dispatchMessage(author, msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Connection closed.");
        }

        teardown();
    }

    private void showUsers() {
        write("Connected users: ");
        getOnShowUsersCallback().execute();
    }

    public void teardown() {
        try {
            reader.close();
            writer.close();
            socket.close();

            onQuit.execute();
        } catch (IOException e) {

        }
    }

    public void write(String msg) {
        writer.println(msg);
        writer.flush();
    }

    public void write(Message msg) {
        write(msg.author + ": " + msg.line);
    }

    public void dispatchMessage(String author, String message) {
        sendMessage(new Message(author, message, System.currentTimeMillis()));
    }

    public void sendMessage(Message msg) {
        try {
            queue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void onShowUsers(Callback callback) {
        this.setOnShowUsersCallback(callback);
    }

    public void onQuit(Callback callback) {
        this.onQuit = callback;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String userName) {
        author = userName;
    }

    public void setThread(Thread t) {
        thread = t;
    }

    public Thread getThread() {
        return thread;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void onQueryName(Callback callback) {
        onQueryName = callback;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public Callback getOnShowUsersCallback() {
        return onShowUsersCallback;
    }

    public void setOnShowUsersCallback(Callback onShowUsersCallback) {
        this.onShowUsersCallback = onShowUsersCallback;
    }
}
