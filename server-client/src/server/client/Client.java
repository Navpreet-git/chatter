package server.client;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;

public class Client {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    private JFrame frame;
    private JTextArea textArea;
    private JTextField textField;
    private JComboBox<String> recipientComboBox; // Dropdown for recipient selection
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

        recipientComboBox = new JComboBox<>();
        recipientComboBox.setFont(new Font("Verdana", Font.PLAIN, 14));

        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(recipientComboBox, BorderLayout.WEST);
        bottomPanel.add(textField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void sendMessage() {
        String recipient = (String) recipientComboBox.getSelectedItem();
        String userMessage = textField.getText().trim();

        if (!userMessage.isEmpty() && recipient != null) {
            output.println(recipient + ":" + userMessage); // Send message to selected recipient
            textArea.append("You to " + recipient + ": " + userMessage + "\n");
            textField.setText(""); // Clear the input field
        } else {
            showError("Message cannot be empty or recipient not selected!");
        }
    }

    private class ReceiveMessagesHandler implements Runnable {
        @Override
        public void run() {
            String serverMessage;
            try {
                while ((serverMessage = input.readLine()) != null) {
                    if (serverMessage.startsWith("CLIENT_LIST:")) {
                        // Update the recipient combo box with the list of clients
                        updateRecipientList(serverMessage);
                    } else {
                        textArea.append(serverMessage + "\n");
                    }
                }
            } catch (IOException e) {
                showError("Connection closed: " + e.getMessage());
            } finally {
                closeConnection();
            }
        }
    }

    private void updateRecipientList(String clientListMessage) {
        String[] clients = clientListMessage.replace("CLIENT_LIST:", "").split(",");
        recipientComboBox.removeAllItems();
        for (String client : clients) {
            recipientComboBox.addItem(client); // Add each client to the dropdown
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

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
        String host = "127.0.0.1";
        int port = 5555;
        new Client(host, port);
    }
}
