package jun;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.*;
import java.io.*;

public class Client extends Application {
    private Socket server_sock = null;
    private PrintWriter OutStream = null;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        // UI setup first
        primaryStage.setTitle("Turtle Grahics");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setVgap(16);
        grid.setHgap(20);

        // Entry fields
        // IP label
        Label IPLable = new Label("IP:");
        GridPane.setConstraints(IPLable, 0, 0);

        // IP input
        TextField IPinput = new TextField("");
        GridPane.setConstraints(IPinput, 1, 0);

        // Port label
        Label PortLable = new Label("Port:");
        GridPane.setConstraints(PortLable, 0, 1);

        // Port input
        TextField Portinput = new TextField("");
        GridPane.setConstraints(Portinput, 1, 1);

        // Length label
        Label LengthLable = new Label("Length:");
        GridPane.setConstraints(LengthLable, 0, 2);

        // Port input
        TextField Lengthinput = new TextField("");
        GridPane.setConstraints(Lengthinput, 1, 2);


        // Buttons
        Button Up, Down, North, South, East, West, Connect;

        Up = new Button(" Up ");
        GridPane.setConstraints(Up, 1, 3);

        Down = new Button("Down");
        GridPane.setConstraints(Down, 4, 3);

        North = new Button("North");
        GridPane.setConstraints(North, 1, 4);

        South = new Button("South");
        GridPane.setConstraints(South, 4, 4);

        East = new Button("East");
        GridPane.setConstraints(East, 1, 5);

        West = new Button("West");
        GridPane.setConstraints(West, 4, 5);

        Connect = new Button("Connect");
        GridPane.setConstraints(Connect, 2, 6);

        // Button Actions
        Connect.setOnAction(e->initConnection(IPinput.getText(), Portinput.getText()));
        North.setOnAction(e->direction("N", Lengthinput.getText()));
        East.setOnAction(e->direction("E", Lengthinput.getText()));
        South.setOnAction(e->direction("S", Lengthinput.getText()));
        West.setOnAction(e->direction("W", Lengthinput.getText()));
        Up.setOnAction(e->penCommand(true));
        Down.setOnAction(e->penCommand(false));

        // Setup the scene
        grid.getChildren().addAll(IPLable, IPinput, PortLable, Portinput, LengthLable, Lengthinput, Up, Down, East,
                West, South, North, Connect);

        Scene scene = new Scene(grid, 600, 400);
        primaryStage.setScene(scene);

        // And display it
        primaryStage.show();
    }

    private void initConnection(String IPEntry, String PortEntry) {
        // Clean up any old connections
        closeConnection();
        try {
            // Create the socket
            int Port = Integer.parseInt(PortEntry);
            server_sock = new Socket(InetAddress.getByName(IPEntry), Port);
            // Buffered output stream for talking to the server
            OutStream = new PrintWriter(server_sock.getOutputStream(), true);
            // Error handling. If any part fails then we have to close the whole connection to clean up
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid Port number.");
            closeConnection();
        } catch (UnknownHostException e) {
            System.err.println("Error: Invalid IP address.");
            closeConnection();
        } catch (IOException e) {
            System.err.println("Error: Unable to get I/O connection for " + IPEntry + ".");
            closeConnection();
        }
    }

    private void closeConnection() {
        // Close the OutStream if it exists
        if (OutStream != null) {
            OutStream.flush();
            OutStream.close();
            OutStream = null;
        }
        // Close the socket if it exists
        if (server_sock != null) {
            try {
                server_sock.close();
            } catch (IOException e) {
                System.err.println("Unable to get I/O connection for open connection.");
                System.exit(1);
            }
            server_sock = null;
        }
    }

    private void direction(String direction, String LengthEntry) {
        // First make sure we have a valid stream to write to
        if (OutStream == null || server_sock == null) {
            System.err.println("Invalid server connection, closing.");
            closeConnection();
            return;
        }
        int length;
        try {
            // Get the length
            length = Integer.parseInt(LengthEntry);
        } catch (NumberFormatException e) {
            System.err.println("Error: " + LengthEntry + "is not a number.");
            return;
        }
        // And print out our direction concatenated with the length to move followed by a new line,
        // i.e. North seven becomes N7\n
        OutStream.println(direction + length);
    }

    private void penCommand(boolean up) {
        // Make sure we have a valid stream to write to
        if (OutStream == null || server_sock == null) {
            System.err.println("Invalid server connection, closing.");
            closeConnection();
            return;
        }
        // U\n means lift the pen, D\n means drop the pen
        if (up) {
            OutStream.println("U");
        } else {
            OutStream.println("D");
        }
    }
}
