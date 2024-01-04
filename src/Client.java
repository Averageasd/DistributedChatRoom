import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Client extends Application {

    private TextArea textArea;
    private TextField textField;

    private String userName;

    @Override
    public void start(Stage stage) throws Exception {
        textArea = new TextArea();
        textArea.setEditable(false);
        textField = new TextField();
        userName = "";
        ScrollPane scrollPane = new ScrollPane(textArea);
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(scrollPane);
        borderPane.setBottom(textField);
        stage.setScene(new Scene(borderPane, 500, 500));
        stage.setTitle("Client");
        stage.show();
        Socket socket = new Socket("localhost", 8000);
        int data = new DataInputStream(socket.getInputStream()).readInt();
        userName = "User" + data + ": ";
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
        ObjectInputStream fromServer = new ObjectInputStream((socket.getInputStream()));
        textField.setOnAction(e -> {
            printWriter.println(userName + textField.getText());
            textField.clear();
            printWriter.flush();
        });

        new Thread(() -> {
            while (true) {
                try {
                    List<String> messages = (ArrayList<String>) fromServer.readObject();
                    System.out.println(messages);
                    Platform.runLater(() -> {
                        textArea.clear();
                        for (String message : messages) {
                            textArea.appendText(message + '\n');
                        }
                    });

                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}