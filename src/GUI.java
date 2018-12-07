import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.text.MessageFormat;

class GUI extends JFrame {
    private final Color background = new Color(238, 238, 238);

    private JMenuItem dh;
    private JButton send;
    private Client client;
    private JMenuItem srp;
    private JMenuItem rsa;
    private JTextArea log;
    private JMenu cryptographicProtocol;
    private JButton connect;
    private JButton refresh;
    private JTextField input;
    private JTextArea status0;
    private JTextArea status1;
    private JScrollPane clients;
    private JList<String> stringJList;
    private DefaultListModel<String> listModel;
    private JMenuItem about;

    GUI(Client client) {
        super(client.getName());
        this.client = client;
        createGUI();
    }

    JTextArea getLog() {
        return log;
    }

    DefaultListModel<String> getListModel() {
        return listModel;
    }

    JList<String> getStringJList() {
        return stringJList;
    }

    JMenu getCryptographicProtocol() {
        return cryptographicProtocol;
    }

    JButton getConnect() {
        return connect;
    }

    JButton getSend() {
        return send;
    }

    JButton getRefresh() {
        return refresh;
    }

    JScrollPane getClients() {
        return clients;
    }

    JTextArea getStatus0() {
        return status0;
    }

    JTextArea getStatus1() {
        return status1;
    }

    private void createGUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel();
        centerPanel.setBorder(BorderFactory.createTitledBorder(Main.LogName + ":"));
        centerPanel.setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());

        JPanel southPanel = new JPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        southPanel.setLayout(gridBagLayout);
        southPanel.setBorder(BorderFactory.createEtchedBorder());

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridheight = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridwidth = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
        gridBagConstraints.insets = new Insets(1, 3, 0, 0);
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.weightx = 100.;
        gridBagConstraints.weighty = 1.;

        input = new JTextField();
        input.setEditable(true);
        gridBagLayout.setConstraints(input, gridBagConstraints);
        southPanel.add(input);

        gridBagConstraints.weightx = 1.;
        gridBagConstraints.weighty = 1.;
        send = new JButton(Main.SendName);
        gridBagLayout.setConstraints(send, gridBagConstraints);
        send.setEnabled(false);
        southPanel.add(send);

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createTitledBorder(Main.StatusName + ":"));
        leftPanel.add(BorderLayout.NORTH, statusPanel);

        status0 = new JTextArea();
        status1 = new JTextArea();
        status0.setBackground(background);
        status1.setBackground(background);
        status0.setEditable(false);
        status1.setEditable(false);
        status0.setLineWrap(false);
        status1.setLineWrap(false);
        statusPanel.add(BorderLayout.WEST, status0);
        statusPanel.add(BorderLayout.CENTER, status1);

        listModel = new DefaultListModel<>();
        stringJList = new JList<>(listModel);
        listModel.add(0, Main.ClientsNo);
        clients = new JScrollPane(stringJList);
        clients.getViewport().getView().setEnabled(false);
        clients.getViewport().getView().setBackground(background);
        clients.setBorder(BorderFactory.createTitledBorder(Main.ClientsName + ":"));
        leftPanel.add(BorderLayout.CENTER, clients);

        JPanel buttonLeftSouthPanel = new JPanel();
        leftPanel.add(BorderLayout.SOUTH, buttonLeftSouthPanel);

        log = new JTextArea();
        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(log);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        centerPanel.add(scrollPane);

        connect = new JButton(Main.ConnectName);
        buttonLeftSouthPanel.add(BorderLayout.SOUTH, connect);

        refresh = new JButton(Main.RefreshName);
        buttonLeftSouthPanel.add(BorderLayout.SOUTH, refresh);

        mainPanel.add(BorderLayout.SOUTH, southPanel);
        mainPanel.add(BorderLayout.CENTER, centerPanel);
        mainPanel.add(BorderLayout.WEST, leftPanel);
        setContentPane(mainPanel);

        setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2));
        setJMenuBar(createMenu());
        listeners();
        pack();
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(getSize());
        connect.setMinimumSize(connect.getSize());
    }

    private JMenuBar createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu settings = new JMenu(Main.Settings);
        cryptographicProtocol = new JMenu(Main.CryptographicProtocol);
        ButtonGroup cryptographicProtocolButtons = new ButtonGroup();
        JMenu question = new JMenu(Main.Question);

        dh = new JCheckBoxMenuItem(Main.dh);
        srp = new JCheckBoxMenuItem(Main.SRP);
        rsa = new JCheckBoxMenuItem(Main.RSA);

        about = new JMenuItem(Main.About);

        cryptographicProtocolButtons.add(dh);
        cryptographicProtocolButtons.add(srp);
        cryptographicProtocolButtons.add(rsa);
        cryptographicProtocol.add(dh);
        cryptographicProtocol.add(srp);
        cryptographicProtocol.add(rsa);

        question.add(about);

        settings.add(cryptographicProtocol);
        menuBar.add(settings);
        menuBar.add(question);

        dh.setSelected(true);
        return menuBar;
    }

    private void send(String text) {
        log.append(MessageFormat.format("{0}:\n{1}\n", client.getName(), text));
        input.setText("");
    }

    private void listeners() {
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 10) {
                    if (client.send(input.getText())) {
                        send(input.getText());
                    }
                }
            }
        });

        send.addActionListener(e -> {
            if (client.send(input.getText())) {
                send(input.getText());
            }
        });

        stringJList.addListSelectionListener(e -> {
            Object selected = stringJList.getSelectedValue();
            if (selected != null) {
                client.setChoosedName(selected.toString());
                client.setChoosedPort(client.getPeers().get1Arg(selected.toString()));
            }
        });

        connect.addActionListener(e -> {
            if (client.IsDisconnected()) {
                if (client.getChoosedName() != null) {
                    if (client.connect()) {
                        client.setIsConnected(true);
                        connect.setText(Main.DisconnectName);
                        refresh.setEnabled(false);
                        cryptographicProtocol.setEnabled(false);
                        clients.getViewport().getView().setEnabled(false);
                        send.setEnabled(true);
                        client.status();
                    } else {
                        try {
                            if (!client.getCrutch()) {
                                client.interrupt = true;
                                client.getOutLink().writeUTF("stop");
                                client.getOutLink().flush();
                            } else client.setCrutch(false);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(GUI.this,
                            Main.NeedChooseUser,
                            Main.Error,
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                if (client.disconnect()) {
                    client.setIsConnected(false);
                    connect.setText(Main.ConnectName);
                    refresh.setEnabled(true);
                    cryptographicProtocol.setEnabled(true);
                    clients.getViewport().getView().setEnabled(true);
                    send.setEnabled(false);
                    input.setText("");
                    client.status();
                }
            }
        });

        refresh.addActionListener(e -> client.refresh());

        dh.addActionListener(e -> {
            client.setCryptographicProtocol(CryptographicProtocol.DH);
            client.status();
        });

        srp.addActionListener(e -> {
            client.setCryptographicProtocol(CryptographicProtocol.SRP);
            client.status();
        });

        rsa.addActionListener(e -> {
            client.setCryptographicProtocol(CryptographicProtocol.RSA);
            client.status();
        });

        about.addActionListener(e -> JOptionPane.showMessageDialog(GUI.this,
                new String[]{"Разработал Афатов Дмитрий В.",
                        "ИВБО-04-15, МИРЭА, 2018"},
                "О программе, v0.2.1",
                JOptionPane.INFORMATION_MESSAGE));
    }
}
