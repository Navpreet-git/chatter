package server.client;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;

// GUI Client class to manage the chat interface
public class Client {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    private JFrame frame;
    private JTextArea textArea;
    private JTextField textField;
    private JButton sendButton;

    public Client(String host, int port) {
        try {
            socket = new Socket(host, port);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            // Initialize GUI
            initializeGUI();

            // Start a thread to handle incoming messages from the server
            new Thread(new ReceiveMessagesHandler()).start();
        } catch (IOException e) {
            showError("Unable to connect to server: " + e.getMessage());
        }
    }

    private void initializeGUI() {
        frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Verdana", Font.PLAIN, 14));
        textArea.setBackground(Color.LIGHT_GRAY);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);

        textField = new JTextField();
        textField.setFont(new Font("Verdana", Font.PLAIN, 14));
        textField.addActionListener(e -> sendMessage());

        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(textField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        
        frame.setVisible(true);
    }

    // Method to send messages to the server
    private void sendMessage() {
        String userMessage = textField.getText();
        if (!userMessage.trim().isEmpty()) {
            output.println(userMessage);
            textArea.append("You: " + userMessage + "\n"); // Display sent message
            textField.setText(""); // Clear the input field
        } else {
            showError("Message cannot be empty!");
        }
    }

    // Thread to handle receiving messages from the server
    private class ReceiveMessagesHandler implements Runnable {
        @Override
        public void run() {
            String serverMessage;
            try {
                while ((serverMessage = input.readLine()) != null) {
                    textArea.append("Other: " + serverMessage + "\n");
                }
            } catch (IOException e) {
                showError("Connection closed: " + e.getMessage());
            } finally {
                closeConnection();
            }
        }
    }

    // Show error messages in a dialog
    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Close connection and clean up
    private void closeConnection() {
        try {
            if (output != null) output.close();
            if (input != null) input.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            showError("Error closing connection: " + e.getMessage());
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        String host = "127.0.0.1"; // Change this to the server's IP if needed
        int port = 5555; // Match the server port
        new Client(host, port);
    }
}
