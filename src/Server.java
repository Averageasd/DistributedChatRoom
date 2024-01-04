import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server extends Application {

    private TextArea textArea;

    private int userCounter;

    private MessageLogger messageLogger;

    private List<ObjectOutputStream> sockets;

    @Override
    public void start(Stage stage) throws Exception {

        userCounter = 0;
        sockets = new ArrayList<>();
        textArea = new TextArea();
        textArea.setEditable(false);
        ScrollPane scrollPane = new ScrollPane(textArea);
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(scrollPane);
        messageLogger = new MessageLogger();
        stage.setTitle("Server");
        stage.setScene(new Scene(borderPane, 500, 500));
        stage.show();

        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(8000);
                while (true) {
                    Socket socket = serverSocket.accept();
                    new DataOutputStream(socket.getOutputStream()).writeInt(userCounter);
                    Platform.runLater(() -> {
                        textArea.appendText("User " + userCounter + " joined" + '\n');
                        userCounter += 1;
                    });
                    new Thread(new HandleClient(socket)).start();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    class HandleClient implements Runnable {

        private final Socket socket;
        protected ObjectOutputStream toClient;
        protected BufferedReader fromClient;

        public HandleClient(Socket socket) {
            this.socket = socket;
            try {
                toClient = new ObjectOutputStream((this.socket.getOutputStream()));
                fromClient = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                ServerLocks.SOCKET_LIST_LOCK.lock();
                sockets.add(toClient);
                ServerLocks.SOCKET_LIST_LOCK.unlock();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            try {
                sendMessageToNewUser();
                while (true) {
                    String message = fromClient.readLine();
                    ServerLocks.MESSAGE_LOGGER_LOCK.lock();
                    messageLogger.messages.add(message);
                    sendMessagesToAll();
                    ServerLocks.MESSAGE_LOGGER_LOCK.unlock();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void sendMessagesToAll() throws IOException {
            ServerLocks.SOCKET_LIST_LOCK.lock();
            for (ObjectOutputStream messageSocket : sockets) {
                messageSocket.reset();
                messageSocket.writeObject(messageLogger.messages);
            }
            ServerLocks.SOCKET_LIST_LOCK.unlock();
        }

        private void sendMessageToNewUser() throws IOException {
            ServerLocks.MESSAGE_LOGGER_LOCK.lock();
            toClient.reset();
            toClient.writeObject(messageLogger.messages);
            ServerLocks.MESSAGE_LOGGER_LOCK.unlock();
        }
    }
}

