package server.client;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 5555;
    private static Set<ClientHandler> clientHandlers = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String username = in.readLine();

                System.out.println(username + " connected");

                ClientHandler clientHandler = new ClientHandler(clientSocket, username, handler -> {
                    clientHandlers.add(handler);
                    broadcastClientList();
                    broadcastMessage(username + " has joined the chat."); // Broadcast connection
                });

                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendPrivateMessage(String message, ClientHandler sender, String recipientUsername) {
        synchronized (clientHandlers) {
            boolean foundRecipient = false;

            for (ClientHandler clientHandler : clientHandlers) {
                if (clientHandler.getUsername().equals(recipientUsername)) {
                    clientHandler.sendMessage("From " + sender.getUsername() + ": " + message);
                    foundRecipient = true;
                    break;
                }
            }

            if (!foundRecipient) {
                sender.sendMessage("User " + recipientUsername + " not found.");
            }
        }
    }

    public static void removeClient(ClientHandler clientHandler) {
        synchronized (clientHandlers) {
            clientHandlers.remove(clientHandler);
            System.out.println("Client " + clientHandler.getUsername() + " disconnected");
            broadcastMessage(clientHandler.getUsername() + " has left the chat."); // Broadcast disconnection
            broadcastClientList();
        }
    }

    private static void broadcastClientList() {
        synchronized (clientHandlers) {
            StringBuilder clientList = new StringBuilder("CLIENT_LIST:");
            for (ClientHandler clientHandler : clientHandlers) {
                clientList.append(clientHandler.getUsername()).append(",");
            }
            String clientListMessage = clientList.toString();

            for (ClientHandler clientHandler : clientHandlers) {
                clientHandler.sendMessage(clientListMessage);
            }
        }
    }

    // Method to broadcast a general message to all clients
    public static void broadcastMessage(String message) {
        synchronized (clientHandlers) {
            for (ClientHandler clientHandler : clientHandlers) {
                clientHandler.sendMessage(message);
            }
        }
    }
}
