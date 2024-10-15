/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.client;

/**
 *
 * @author Nkaur
 */
import java.io.*;
import java.net.*;

public class Client {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private BufferedReader keyboardInput;

    public Client(String host, int port) {
        try {
            socket = new Socket(host, port);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            keyboardInput = new BufferedReader(new InputStreamReader(System.in));

            // Start a thread to handle incoming messages from the server
            new Thread(new ReceiveMessagesHandler()).start();

            // Main thread will handle sending messages to the server
            String userMessage;
            while ((userMessage = keyboardInput.readLine()) != null) {
                output.println(userMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Thread to handle receiving messages from the server
    private class ReceiveMessagesHandler implements Runnable {
        @Override
        public void run() {
            String serverMessage;
            try {
                while ((serverMessage = input.readLine()) != null) {
                    System.out.println("outside: " + serverMessage);
                }
            } catch (IOException e) {
                System.out.println("Connection closed");
            }
        }
    }

    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 5555;
        new Client(host, port);
    }
}

