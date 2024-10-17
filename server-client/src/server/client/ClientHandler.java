package server.client;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private OnClientReadyListener listener; // Add a listener interface

    // Constructor now accepts a listener
    public ClientHandler(Socket socket, String username, OnClientReadyListener listener) {
        this.socket = socket;
        this.username = username;
        this.listener = listener; // Store the listener
    }

    @Override
    public void run() {
        try {
            // Initialize input and output streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Send the assigned username to the client
            out.println("You are connected as: " + username);

            // Notify the server that this client is ready
            listener.onClientReady(this);

            String clientMessage;

            // Keep listening for messages from the client
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("Received from " + username + ": " + clientMessage);

                // Expect the message format to be "recipientUsername:message"
                if (clientMessage.contains(":")) {
                    String[] parts = clientMessage.split(":", 2);
                    String recipientUsername = parts[0].trim();
                    String message = parts[1].trim();

                    // Send the message to the specified recipient
                    Server.sendPrivateMessage(message, this, recipientUsername);
                } else {
                    out.println("Invalid message format. Use recipientUsername:message");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Clean up and close resources
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Remove the client from the server's list
            Server.removeClient(this);
        }
    }

    public String getUsername() {
        return username;
    }

    // Method to send a message to this client
    public void sendMessage(String message) {
        out.println(message);
    }

    // Listener interface to notify when the client is ready
    public interface OnClientReadyListener {
        void onClientReady(ClientHandler clientHandler);
    }
}
