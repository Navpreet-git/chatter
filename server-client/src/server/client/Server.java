package server.client;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 5555;
    private static Set<ClientHandler> clientHandlers = Collections.synchronizedSet(new HashSet<>());
    private static int clientCount = 0;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientCount++;
                String username = getOrdinalName(clientCount);

                System.out.println(username + " connected");

                // Create a new ClientHandler and provide the listener
                ClientHandler clientHandler = new ClientHandler(clientSocket, username, handler -> {
                    clientHandlers.add(handler); // Add the client only after it's ready
                    broadcastClientList(); // Broadcast client list after the client is ready
                });

                // Start the new client handler in a separate thread
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getOrdinalName(int number) {
        return number + "th";
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
}
