/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.client;

/**
 *
 * @author Nkaur
 */import java.io.*;
import java.net.*;

// A separate class that handles each client in a dedicated thread
public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Initialize input and output streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String clientMessage;

            // Keep listening for messages from the client
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("Received: " + clientMessage);
                
                // Broadcast the message to other clients
                Server.broadcastMessage(clientMessage, this);
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

    // Method to send a message to this client
    public void sendMessage(String message) {
        out.println(message);
    }
}
