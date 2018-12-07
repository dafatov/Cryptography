import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

class Client {
    private final DoubleSideMap<Integer, String> peers = new DoubleSideMap<>();
    private DiffieHellman diffieHellman;
    private RSA rsa;
    private SRP srp;
    private GUI gui;
    private int port;
    private String login;
    private String password;
    private boolean isLogin;
    private Socket link;
    private String name;
    private Server server;
    private int choosedPort;
    private String choosedName;
    private DataInputStream inLink;
    private DataOutputStream outLink;
    boolean interrupt = false;
    private boolean isConnected = false;
    private CryptographicProtocol cryptographicProtocol = CryptographicProtocol.SRP;
    private boolean crutch = false;

    Client(String name, int port) {
        this.name = name;
        this.port = port;
        gui = new GUI(this);
        status();

        server = new Server(this);
        Thread serverThread = new Thread(server, "Server" + name);
        serverThread.setDaemon(true);
        serverThread.start();
    }

    boolean getCrutch() {
        return crutch;
    }

    void setCrutch(boolean crutch) {
        this.crutch = crutch;
    }

    DiffieHellman getDiffieHellman() {
        return diffieHellman;
    }

    RSA getRsa() {
        return rsa;
    }

    SRP getSrp() {
        return srp;
    }

    DataOutputStream getOutLink() {
        return outLink;
    }

    void setDiffieHellman(DiffieHellman diffieHellman) {
        this.diffieHellman = diffieHellman;
    }

    void setRsa(RSA rsa) {
        this.rsa = rsa;
    }

    void setSrp(SRP srp) {
        this.srp = srp;
    }

    boolean IsDisconnected() {
        return !isConnected;
    }

    void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    void setLink(Socket link) {
        this.link = link;
    }

    void setInLink(DataInputStream inLink) {
        this.inLink = inLink;
    }

    void setOutLink(DataOutputStream outLink) {
        this.outLink = outLink;
    }

    GUI getGui() {
        return gui;
    }

    void setChoosedPort(int choosedPort) {
        this.choosedPort = choosedPort;
    }

    String getChoosedName() {
        return choosedName;
    }

    void setChoosedName(String choosedName) {
        this.choosedName = choosedName;
    }

    DoubleSideMap<Integer, String> getPeers() {
        return peers;
    }

    CryptographicProtocol getCryptographicProtocol() {
        return cryptographicProtocol;
    }

    void setCryptographicProtocol(CryptographicProtocol cryptographicProtocol) {
        this.cryptographicProtocol = cryptographicProtocol;
    }

    String getName() {
        return name;
    }

    int getPort() {
        return port;
    }

    void refresh() {
        CountDownLatch latch = new CountDownLatch(Main.EndPortInterval - Main.BeginPortInterval);
        List<String> authed = new ArrayList<>();
        gui.getListModel().clear();
        peers.clear();

        for (int i = Main.BeginPortInterval; i <= Main.EndPortInterval; i++) {
            if (i != port) {
                int finalI = i;

                Thread thread = new Thread(() -> {
                    try {
                        Socket socket = new Socket("127.0.0.1", finalI);
                        socket.setSoTimeout(500);
                        DataInputStream in = new DataInputStream(socket.getInputStream());
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                        out.writeUTF("auth");
                        out.flush();

                        if (!in.readUTF().equals("authed"))
                            return;

                        String name = in.readUTF();
                        synchronized (authed) {
                            authed.add(name);
                        }
                        synchronized (peers) {
                            peers.put(finalI, name);
                        }

                        out.writeUTF(getName());
                        out.flush();

                        out.writeUTF(String.valueOf(port));
                        out.flush();

                        out.writeUTF("stop");
                        out.flush();
                    } catch (IOException ignored) {
                    }
                    latch.countDown();
                });
                thread.setDaemon(true);
                thread.start();
            }
        }
        try {
            latch.await();
            if (!authed.isEmpty()) {
                if (authed.size() > 1) {
                    authed.sort(Comparator.naturalOrder());
                }
                for (String i : authed) {
                    gui.getListModel().addElement(i);
                }
                gui.getClients().getViewport().getView().setEnabled(true);
            } else {
                gui.getListModel().addElement(Main.ClientsNo);
            }
        } catch (InterruptedException e) {
            System.err.println("Какая-то лажа в Client.refresh():");
            e.printStackTrace();
        }
    }

    boolean connect() {
        if (getCryptographicProtocol().equals(CryptographicProtocol.SRP)) {
            if (!initRegInfo()) {
                crutch = true;
                return false;
            }
        }
        try {
            link = new Socket("127.0.0.1", Main.EnvironmentPort);
            inLink = new DataInputStream(link.getInputStream());
            outLink = new DataOutputStream(link.getOutputStream());

            server.setNoNeedAccept();

            server.setSocket(link);
            server.setIn(inLink);
            server.setOut(outLink);
            server.setConnectedName(choosedName);

            outLink.writeUTF(String.valueOf(choosedPort));
            outLink.flush();

            outLink.writeUTF("connect");
            outLink.flush();

            if (!inLink.readUTF().equals("free")) {
                JOptionPane.showMessageDialog(gui,
                        new String[]{Main.BusyServerConnection},
                        Main.Error,
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            outLink.writeUTF(cryptographicProtocol.toString());
            outLink.flush();

            String serverProtocol = inLink.readUTF();
            if (!serverProtocol.equals(cryptographicProtocol.toString())) {
                JOptionPane.showMessageDialog(gui,
                        new String[]{Main.WrongProtocol + serverProtocol},
                        Main.Error,
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if (cryptographicProtocol.equals(CryptographicProtocol.DH)) {
                if (connectDH()) return true;
            } else if (cryptographicProtocol.equals(CryptographicProtocol.SRP)) {
                if (connectSRP()) return true;
            } else if (cryptographicProtocol.equals(CryptographicProtocol.RSA)) {
                if (connectRSA()) return true;
            } else {
                return false;
            }
        } catch (
                IOException e2) {
            return false;
        }
        return false;
    }

    private boolean connectDH() {
        diffieHellman = new DiffieHellman();
        try {
            outLink.writeUTF("P:" + String.valueOf(diffieHellman.getP()));
            outLink.flush();
            outLink.writeUTF("A:" + String.valueOf(diffieHellman.getA()));
            outLink.flush();

            if (!diffieHellman.pickSecretKey()) return false;
            diffieHellman.calcPublicKey();

            diffieHellman.setSharedPublicKey(Long.parseLong(inLink.readUTF().split(":")[1]));
            outLink.writeUTF("PublicKey:" + String.valueOf(diffieHellman.getPublicKey()));
            outLink.flush();

            diffieHellman.calcSharedSecretKey();
        } catch (IOException io) {
            io.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean connectSRP() {
        srp = new clientSRP(isLogin, password);
        try {
            outLink.writeUTF(isLogin + ":" + login);
            outLink.flush();//1

            if (inLink.readUTF().equals("ok")) {//2
                if (isLogin) {
                    outLink.writeUTF("A:" + String.valueOf(srp.getA()));
                    outLink.flush();//3
                    if (inLink.readUTF().equals("no")) {//4
                        return false;
                    }
                    srp.setB(Long.parseLong(inLink.readUTF().split(":")[1]));//5
                    srp.calculateU();
                    if (srp.getB() == 0 || srp.getU() == 0) {
                        outLink.writeUTF("no");
                        outLink.flush();//6
                        return false;
                    } else {
                        outLink.writeUTF("ok");
                        outLink.flush();//6
                        if (inLink.readUTF().equals("no")) {//7
                            return false;
                        }
                        ((clientSRP) srp).setSalt(inLink.readUTF().split(":")[1]);//8
                        ((clientSRP) srp).calculateK();
                        ((clientSRP) srp).calculateM(login);
                        outLink.writeUTF("M:" + String.valueOf(srp.getM()));
                        outLink.flush();//9
                        if (inLink.readUTF().equals("no")) {//10
                            wrongPassword();
                            return false;
                        }
                        srp.calculateR();
                        if (srp.getR() != Long.parseLong(inLink.readUTF().split(":")[1])) {//11
                            outLink.writeUTF("no");
                            outLink.flush();//12
                            wrongPassword();
                            return false;
                        } else {
                            outLink.writeUTF("ok");
                            outLink.flush();//12
                        }
                    }
                } else {
                    outLink.writeUTF("s:" + ((clientSRP) srp).getSalt());
                    outLink.flush();
                    outLink.writeUTF("v:" + String.valueOf(((clientSRP) srp).getV()));
                    outLink.flush();
                    JOptionPane.showMessageDialog(gui,
                            Main.SuccessRegistration,
                            Main.Registration,
                            JOptionPane.INFORMATION_MESSAGE);
                    return false;
                }
            } else {
                JOptionPane.showMessageDialog(gui,
                        isLogin ? Main.NeedRegistration : Main.NeedIdentification,
                        isLogin ? Main.Identification : Main.Registration,
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void wrongPassword() {
        JOptionPane.showMessageDialog(gui,
                Main.ErrorLogin,
                Main.Authentication,
                JOptionPane.ERROR_MESSAGE);
    }

    private boolean initRegInfo() {
        UIManager.put("OptionPane.yesButtonText", "Вход");
        UIManager.put("OptionPane.noButtonText", "Регистрация");
        int tmp = JOptionPane.showConfirmDialog(gui,
                "",
                Main.Authentication,
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
        if (tmp != 0 && tmp != 1) return false;
        isLogin = tmp == 0;
        UIManager.put("OptionPane.yesButtonText", "Да");
        UIManager.put("OptionPane.noButtonText", "Нет");

        do {
            login = JOptionPane.showInputDialog(gui,
                    Main.EnterLogin,
                    isLogin ? Main.Identification : Main.Registration,
                    JOptionPane.QUESTION_MESSAGE);
            if (login == null) return false;
        } while (!login.matches(Main.LoginRegex));

        do {
            password = JOptionPane.showInputDialog(gui,
                    Main.EnterPassword,
                    isLogin ? Main.Identification : Main.Registration,
                    JOptionPane.QUESTION_MESSAGE);
            if (password == null) return false;
        } while (!password.matches(Main.PasswordRegex));
        return true;
    }

    private boolean connectRSA() {
        rsa = new RSA();
        try {
            outLink.writeUTF("e:" + String.valueOf(rsa.getE()));
            outLink.flush();
            outLink.writeUTF("n:" + String.valueOf(rsa.getN()));
            outLink.flush();

            rsa.setRecievedE(Long.parseLong(inLink.readUTF().split(":")[1]));
            rsa.setRecievedN(Long.parseLong(inLink.readUTF().split(":")[1]));
        } catch (IOException io) {
            return false;
        }
        return true;
    }

    boolean disconnect() {
        try {
            if (cryptographicProtocol.equals(CryptographicProtocol.DH)) {
                if (!disconnectDH()) return false;
            } else if (cryptographicProtocol.equals(CryptographicProtocol.SRP)) {
                if (!disconnectSRP()) return false;
            } else if (cryptographicProtocol.equals(CryptographicProtocol.RSA)) {
                if (!disconnectRSA()) return false;
            } else {
                return false;
            }

            outLink.writeUTF("disconnect");
            outLink.flush();

            interrupt = true;
            outLink.writeUTF("stop");
            outLink.flush();

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean disconnectDH() {
        diffieHellman = null;
        return true;
    }

    private boolean disconnectSRP() {
        srp = null;
        return true;
    }

    private boolean disconnectRSA() {
        rsa = null;
        return true;
    }

    boolean send(String message) {
        try {
            outLink.writeUTF("msg");
            outLink.flush();

            String string;
            if (cryptographicProtocol.equals(CryptographicProtocol.DH)) {
                string = Coder.code(diffieHellman.getSharedSecretKey(), message);
            } else if (cryptographicProtocol.equals(CryptographicProtocol.SRP)) {
                string = Coder.code(srp.getK(), message);
            } else if (cryptographicProtocol.equals(CryptographicProtocol.RSA)) {
                string = Coder.codeRSA(rsa.getRecievedE(), rsa.getRecievedN(), message);
            } else {
                return false;
            }

            outLink.writeUTF(string);
            outLink.flush();
            return true;
        } catch (IOException io) {
            return false;
        }
    }

    void status() {
        String[] status = new String[2];
        if (cryptographicProtocol.equals(CryptographicProtocol.DH)) {
            status[0] = Main.dhStatusNames;
            if (diffieHellman != null)
                status[1] = MessageFormat.format("{0}\n{1}\n{2}\n{3}\n{4}",
                        diffieHellman.getSecretKey(),
                        diffieHellman.getP(),
                        diffieHellman.getA(),
                        diffieHellman.getPublicKey(),
                        diffieHellman.getSharedSecretKey());
            else status[1] = "0\n0\n0\n0\n0";
        } else if (cryptographicProtocol.equals(CryptographicProtocol.SRP)) {
            status[0] = Main.srpStatusNames;
            if (srp != null) {
                status[1] = MessageFormat.format("{0}\n{1}\n{2}\n{3}\n{4}\n{5}",
                        srp.getA(),
                        srp.getB(),
                        srp.getU(),
                        srp.getK(),
                        srp.getM(),
                        srp.getR());
            } else status[1] = "0\n0\n0\n0\n0\n0";
        } else if (cryptographicProtocol.equals(CryptographicProtocol.RSA)) {
            status[0] = Main.rsaStatusNames;
            if (rsa != null) {
                status[1] = MessageFormat.format("{0}\n{1}\n{2}\n{3}\n{4}\n{5}\n{6}\n{7}",
                        rsa.getP(),
                        rsa.getQ(),
                        rsa.getN(),
                        rsa.getF(),
                        rsa.getE(),
                        rsa.getD(),
                        rsa.getRecievedE(),
                        rsa.getRecievedN());
            } else {
                status[1] = "0\n0\n0\n0\n0\n0\n0\n0";
            }
        } else {
            System.err.println("Error in generate status!");
        }

        getGui().getStatus0().setText(status[0]);
        getGui().getStatus1().setText(status[1]);
    }
}

class Server implements Runnable {
    private Client client;
    private ServerSocket serverSocket;
    private String connectedName;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean needAccept = true;

    Server(Client client) {
        this.client = client;
        try {
            serverSocket = new ServerSocket(client.getPort());
        } catch (IOException e) {
            System.err.println("Error in Constructor of Server");
        }
    }

    void setConnectedName(String connectedName) {
        this.connectedName = connectedName;
    }

    void setNoNeedAccept() {
        this.needAccept = false;
    }

    void setSocket(Socket socket) {
        this.socket = socket;
    }

    void setIn(DataInputStream in) {
        this.in = in;
    }

    void setOut(DataOutputStream out) {
        this.out = out;
    }

    private void serverSocketAccept() {
        while (needAccept) {
            try {
                serverSocket.setSoTimeout(100);
                socket = serverSocket.accept();
                needAccept = false;
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void run() {
        try {
            serverSocketAccept();

            client.setLink(socket);
            client.setInLink(in);
            client.setOutLink(out);

            while (true) {
                switch (in.readUTF()) {
                    case "auth":
                        out.writeUTF("authed");
                        out.flush();

                        out.writeUTF(client.getName());
                        out.flush();

                        connectedName = in.readUTF();

                        client.getPeers().put(Integer.parseInt(in.readUTF()), connectedName);
                        if (!client.getGui().getListModel().contains(connectedName)) {
                            if (!client.getGui().getClients().getViewport().getView().isEnabled()) {
                                client.getGui().getListModel().clear();
                            }
                            client.getGui().getListModel().addElement(connectedName);
                        }
                        break;
                    case "connect":
                        if (connect()) {
                            client.getGui().getConnect().setText(Main.DisconnectName);
                            client.getGui().getStringJList().setSelectedValue(connectedName, true);
                            client.getGui().getClients().getViewport().getView().setEnabled(false);
                            client.getGui().getSend().setEnabled(true);
                            client.getGui().getRefresh().setEnabled(false);
                            client.getGui().getCryptographicProtocol().setEnabled(false);
                            client.setIsConnected(true);
                            client.status();
                        }
                        break;
                    case "disconnect":
                        if (disconnect()) {
                            client.getGui().getConnect().setText(Main.ConnectName);
                            client.getGui().getClients().getViewport().getView().setEnabled(true);
                            client.getGui().getSend().setEnabled(false);
                            client.getGui().getRefresh().setEnabled(true);
                            client.getGui().getCryptographicProtocol().setEnabled(true);
                            client.setIsConnected(false);
                            client.status();
                        }
                        break;
                    case "msg":
                        String message = in.readUTF();
                        client.getGui().getLog().append(MessageFormat.format("{0}:\n{1}\n", connectedName, msgDecode(message)));
                        break;
                    case "stop":
                        if (socket.isConnected() && !client.interrupt) {
                            out.writeUTF("stop");
                            out.flush();
                        } else client.interrupt = false;
                        needAccept = true;
                        socket.close();
                        return;
                }
            }
        } catch (IOException e) {
            System.err.println("Error in run() of Server");
        } finally {
            run();
        }
    }

    private boolean connect() {
        try {
            if (client.IsDisconnected()) {
                out.writeUTF("free");
                out.flush();

                String clientProtocol = in.readUTF();
                out.writeUTF(client.getCryptographicProtocol().toString());
                out.flush();
                if (!clientProtocol.equals(client.getCryptographicProtocol().toString())) {
                    return false;
                }

                if (client.getCryptographicProtocol().equals(CryptographicProtocol.DH)) {
                    if (connectDH()) return true;
                } else if (client.getCryptographicProtocol().equals(CryptographicProtocol.SRP)) {
                    if (connectSRP()) return true;
                } else if (client.getCryptographicProtocol().equals(CryptographicProtocol.RSA)) {
                    if (connectRSA()) return true;
                } else {
                    return false;
                }
            } else {
                out.writeUTF("busy");
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error in connect() of Server");
        }
        return false;
    }

    private boolean connectDH() {
        client.setDiffieHellman(new DiffieHellman());
        try {
            client.getDiffieHellman().setP(Long.parseLong(in.readUTF().split(":")[1]));
            client.getDiffieHellman().setA(Long.parseLong(in.readUTF().split(":")[1]));

            if (!client.getDiffieHellman().pickSecretKey()) return false;
            client.getDiffieHellman().calcPublicKey();

            out.writeUTF("PublicKey:" + String.valueOf(client.getDiffieHellman().getPublicKey()));
            out.flush();
            client.getDiffieHellman().setSharedPublicKey(Long.parseLong(in.readUTF().split(":")[1]));

            client.getDiffieHellman().calcSharedSecretKey();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean connectSRP() {
        client.setSrp(new serverSRP());
        try {
            String[] input = in.readUTF().split(":");//1
            if (input[0].equals("true") && ((serverSRP) client.getSrp()).getUsersList().contains(input[1])) {
                out.writeUTF("ok");
                out.flush();//2

                client.getSrp().setA(Long.parseLong(in.readUTF().split(":")[1]));//3
                if (client.getSrp().getA() == 0) {
                    out.writeUTF("no");
                    out.flush();//4
                    return false;
                } else {
                    out.writeUTF("ok");
                    out.flush();//4
                    ((serverSRP) client.getSrp()).calculateB(input[1]);
                    out.writeUTF("B:" + String.valueOf(client.getSrp().getB()));
                    out.flush();//5
                    if (in.readUTF().equals("no")) {//6
                        return false;
                    }
                    client.getSrp().calculateU();
                    if (client.getSrp().getU() == 0) {
                        out.writeUTF("no");
                        out.flush();//7
                        return false;
                    } else {
                        out.writeUTF("ok");
                        out.flush();//7

                        out.writeUTF("salt:" + String.valueOf(((serverSRP) client.getSrp()).getUsersList().get(input[1]).get1Arg(0)));
                        out.flush();//8
                        ((serverSRP) client.getSrp()).calculateK(input[1]);
                        ((serverSRP) client.getSrp()).calculateM(input[1]);
                        if (client.getSrp().getM() != (Long.parseLong(in.readUTF().split(":")[1]))) {//9
                            out.writeUTF("no");
                            out.flush();//10
                            return false;
                        } else {
                            out.writeUTF("ok");
                            out.flush();//10

                            client.getSrp().calculateR();
                            out.writeUTF("R:" + String.valueOf(client.getSrp().getR()));
                            out.flush();//11
                            if (in.readUTF().equals("no")) {//12
                                return false;
                            }
                        }
                    }
                }
            } else if (!input[0].equals("true") && !((serverSRP) client.getSrp()).getUsersList().contains(input[1])) {
                out.writeUTF("ok");
                out.flush();

                ((serverSRP) client.getSrp()).getUsersList().put(input[1], in.readUTF().split(":")[1],
                        Long.parseLong(in.readUTF().split(":")[1]));
                ((serverSRP) client.getSrp()).saveUsersList();
                return false;
            } else {
                out.writeUTF("no");
                out.flush();
                return false;
            }
        } catch (IOException io) {
            io.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean connectRSA() {
        client.setRsa(new RSA());
        try {
            client.getRsa().setRecievedE(Long.parseLong(in.readUTF().split(":")[1]));
            client.getRsa().setRecievedN(Long.parseLong(in.readUTF().split(":")[1]));

            out.writeUTF("e:" + String.valueOf(client.getRsa().getE()));
            out.flush();
            out.writeUTF("n:" + String.valueOf(client.getRsa().getN()));
            out.flush();
        } catch (IOException io) {
            io.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean disconnect() {
        if (client.getCryptographicProtocol().equals(CryptographicProtocol.DH)) {
            return disconnectDH();
        } else if (client.getCryptographicProtocol().equals(CryptographicProtocol.SRP)) {
            return disconnectSRP();
        } else if (client.getCryptographicProtocol().equals(CryptographicProtocol.RSA)) {
            return disconnectRSA();
        } else {
            return false;
        }
    }

    private boolean disconnectDH() {
        client.setDiffieHellman(null);
        return true;
    }

    private boolean disconnectSRP() {
        client.setSrp(null);
        return true;
    }

    private boolean disconnectRSA() {
        client.setRsa(null);
        return true;
    }

    private String msgDecode(String message) {
        switch (client.getCryptographicProtocol()) {
            case DH:
                return Coder.decode(client.getDiffieHellman().getSharedSecretKey(), message);
            case SRP:
                return Coder.decode(client.getSrp().getK(), message);
            case RSA:
                return Coder.decodeRSA(client.getRsa().getD(), client.getRsa().getN(), message);
            default:
                return null;
        }
    }
}