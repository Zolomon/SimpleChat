
package com.zolomon.simplechat.server;

import com.zolomon.simplechat.client.ConnectionThread;
import com.zolomon.simplechat.shared.Callback;
import com.zolomon.simplechat.shared.Message;

import java.awt.List;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class Server implements Runnable {

    ServerSocket server;

    BufferedWriter writer;

    BlockingQueue<Message> queue;

    ConcurrentLinkedDeque<ConnectionThread> connections;

    ConcurrentHashMap<String, ConcurrentLinkedDeque<ConnectionThread>> channels;

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Server server = new Server();

        server.run();
    }

    public Server() {
        connections = new ConcurrentLinkedDeque<ConnectionThread>();
        queue = new LinkedBlockingDeque<Message>();
        writer = new BufferedWriter(new OutputStreamWriter(System.out));
        channels = new ConcurrentHashMap<String, ConcurrentLinkedDeque<ConnectionThread>>();
        try {
            server = new ServerSocket(8888);
        } catch (IOException e1) {
            e1.printStackTrace();
            System.out.println("Could not create a server socket");
        }
    }

    public void run() {
        Thread messageDispatcher = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    removeClosedConnections();

                    Message msg = getMessage();

                    dispatchMessageToClients(msg);
                }
            }

            private Message getMessage() {
                Message msg = null;
                try {
                    msg = queue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
                return msg;
            }

            private void dispatchMessageToClients(Message msg) {
                write(msg);
                for (ConnectionThread c : connections) {
                    if (!c.isClosed()) {
                        c.write(msg);
                    }
                }
            }

            private void removeClosedConnections() {
                ArrayList<ConnectionThread> closedConnections = new ArrayList<ConnectionThread>();
                for (ConnectionThread c : connections) {
                    if (!c.getThread().isAlive()) {
                        closedConnections.add(c);
                    }
                }

                for (ConnectionThread closed : closedConnections) {
                    connections.remove(closed);
                    for (ConnectionThread open : connections) {
                        open.write(new Message("Server", closed.getAuthor() + " has quit.", System
                                .currentTimeMillis()));
                    }
                }
            }

        });

        messageDispatcher.start();
        System.out.println("[Message Dispatcher started]");
        System.out.println("[Starting Connection loop]");
        while (true) {
            try {
            	System.out.println("[Waiting for connections]");
                Socket socket = server.accept();
                System.out.println("[New connection from: "+socket.getInetAddress().getHostAddress()+"@" + socket.getPort()+"]");

                final ConnectionThread ct = createNewConnection(socket);

                connections.add(ct);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Could not create a ConnectionThread.");
            }
        }
    }

	private ConnectionThread createNewConnection(Socket socket) throws IOException {
		final ConnectionThread ct = new ConnectionThread(socket, writer, queue);

		Thread thread = new Thread(ct);
		thread.start();

		ct.setThread(thread);

		ct.onShowUsers(new Callback() {

		    @Override
		    public void execute() {
		        for (ConnectionThread c : connections) {
		            ct.write(new Message("Server", c.getAuthor(), System
		                    .currentTimeMillis()));
		        }
		    }

		});

		ct.onQuit(new Callback() {

		    @Override
		    public void execute() {
		        connections.remove(ct);
		        System.out.println("REMOVED CLIENT@" + ct.getAuthor());
		    }

		});

		ct.onQueryName(new Callback() {

		    @Override
		    public void execute() {
		        try {
		            String userName = null;
		            boolean isTaken = false;
		            do {
		                isTaken = false;
		                ct.write("What is your name?");
		                userName = ct.getReader().readLine();
		                for (ConnectionThread c : connections) {
		                    if (userName.equals(c.getAuthor())) {
		                        isTaken = true;
		                        ct.write("Sorry that name is taken. Please try again.");
		                    }
		                }
		            } while (isTaken);

		            ct.setAuthor(userName);

		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		    }
		});
		return ct;
	}

    private void write(Message msg) {
        try {
            synchronized (writer) {
                writer.write("Sending '" + msg.author + ": " + msg.line + "' to all connections.\n");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
