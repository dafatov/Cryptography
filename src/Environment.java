import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;

class Environment {
    private GUI gui;

    Environment(int port) {
        gui = new GUI();
        listeners();
        Thread clientToServer = new Thread(new ClientToServer(port, this));
        clientToServer.setDaemon(true);
        clientToServer.start();
    }

    class GUI extends JFrame {
        private JTextArea log;

        GUI() {
            super("Внешняя среда");
            createGUI();
        }

        JTextArea getLog() {
            return log;
        }

        private void createGUI() {
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());

            log = new JTextArea("");
            log.setEditable(false);
            log.setLineWrap(true);
            log.setWrapStyleWord(true);
            log.setBorder(new EtchedBorder());
            JScrollPane scrollPane = new JScrollPane(log);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            mainPanel.add(scrollPane);

            setContentPane(mainPanel);
            setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2));
            pack();
            setVisible(true);
            setResizable(false);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setMinimumSize(getSize());
        }
    }

    private void listeners() {
        gui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (replaceLastLog()) System.out.println("Переименован успешно");
                else System.err.println("Ошибка при переименовывании");
            }
        });
    }

    private boolean replaceLastLog() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");
        String[] parts = Main.EnvironmentFile.split("\\\\");
        String[] file = parts[parts.length - 1].split("\\.");
        file[0] = dateFormat.format(date);
        StringBuilder newPath = new StringBuilder();

        for (int i = 0; i < parts.length - 1; i++) {
            newPath.append(parts[i]);
            newPath.append("\\");
        }
        newPath.append(file[0]);
        newPath.append(".");
        newPath.append(file[1]);
        return FileManager.rename(Main.EnvironmentFile, newPath.toString());
    }

    synchronized void print(String s, boolean toServer) {
        if (toServer) {
            FileManager.write(Main.EnvironmentFile, "toServer: " + s + '\n');
            if (Main.LogEnv) System.out.println(s);
            gui.getLog().append("toServer: " + s + '\n');
        } else {
            FileManager.write(Main.EnvironmentFile, "toClient: " + s + '\n');
            if (Main.LogEnv) System.err.println(s);
            gui.getLog().append("toClient:  " + s + '\n');
        }
    }
}

class ClientToServer implements Runnable {
    private ServerSocket serverSocket;
    private Environment e;

    ClientToServer(int port, Environment e) {
        this.e = e;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            System.err.println("Error in Constructor of DownloadClient");
        }
    }

    @Override
    public void run() {
        try {
            Socket socketIn = serverSocket.accept();
            DataInputStream fromClient = new DataInputStream(socketIn.getInputStream());
            DataOutputStream toClient = new DataOutputStream(socketIn.getOutputStream());

            Socket socketOut = new Socket("127.0.0.1", Integer.parseInt(fromClient.readUTF()));
            DataInputStream fromServer = new DataInputStream(socketOut.getInputStream());
            DataOutputStream toServer = new DataOutputStream(socketOut.getOutputStream());

            Thread thread = new Thread(new ServerToClient(fromServer, toClient, e));
            thread.setDaemon(true);
            thread.start();

            do {
                String string = fromClient.readUTF();
                toServer.writeUTF(string);
                toServer.flush();
                e.print(string, true);
            } while (true);
        } catch (EOFException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error in run() of ClientToServer");
        }
        run();
    }

}

class ServerToClient implements Runnable {
    private DataInputStream fromServer;
    private DataOutputStream toClient;
    private Environment e;

    ServerToClient(DataInputStream fromServer, DataOutputStream toClient, Environment e) {
        this.e = e;
        this.fromServer = fromServer;
        this.toClient = toClient;
    }

    @Override
    public void run() {
        try {
            do {
                String string = fromServer.readUTF();
                toClient.writeUTF(string);
                toClient.flush();
                e.print(string, false);
            } while (true);
        } catch (EOFException ignored) {
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Error in run() of ServerToClient");

        }
    }
}