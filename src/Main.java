import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;

public class Main {
    //Text
    final static String SRP = "SRP";
    final static String RSA = "RSA";
    final static String Question = "?";
    final static String LogName = "Лог";
    final static String Error = "Ошибка";
    final static String dh = "Диффи-Хеллман";
    final static String About = "О программе";
    final static String StatusName = "Статус";
    final static String SendName = "Отправить";
    final static String Settings = "Настройки";
    final static String RefreshName = "Обновить";
    final static String Registration = "Регистрация";
    final static String Authentication = "Аутентификация";
    final static String ConnectName = "Подключиться";
    final static String ClientsName = "Пользователи";
    final static String EnterLogin = "Введите логин:";
    final static String DisconnectName = "Отключиться";
    final static String Identification = "Идентификация";
    final static String EnterPassword = "Введите пароль:";
    final static String ClientsNo = "Нет пользователей...";
    final static String NeedRegistration = "Необходимо пройти регистрацию";
    final static String CryptographicProtocol = "Криптографический протокол";
    final static String NeedIdentification = "Необходимо пройти идентификацию";
    final static String WrongProtocol = "Для подключения необходим протокол: ";
    final static String BusyServerConnection = "Искомый пользователь уже подключен";
    final static String[] ErrorLogin = new String[]{"Введенный логин или пароль неверен",
            "Попробуйте пройти идентификацию заново"};
    final static String[] SuccessRegistration = new String[]{"Регистрация прошла успешно",
            "Пройдите идентификацию"};
    final static String[] NeedChooseUser = new String[]{"Не выбран пользователь для подключения",
            "Нажмите \"" + Main.RefreshName + "\" для получения списка пользователей",
            "и выберите пользователя"};

    //Regex
    final static String LoginRegex = "([a-z]|[A-Z])([a-z]|[A-Z]|[0-9]){2,19}";
    final static String PasswordRegex = "([a-z]|[A-Z]|[0-9]){3,20}";

    final static int limitLoops = 100;
    final static Long[] SimpleNumbers = simpleGen(1000);

    //Status
    final static String dhStatusNames = "Секретный ключ: \nОбщий параметр P: \nОбщий параметр A: \nПубличный ключ: \nОбщий секретный ключ: ";
    final static String rsaStatusNames = "P: \nQ: \nМодуль: \nФункция Эйлера: \nОткрытая экспонента: \nD: \nПолученная экспонента: \nПолученный модуль: ";
    final static String srpStatusNames = "A: \nB: \nu: \nK: \nM: \nR: ";

    //Ports
    final static int EnvironmentPort = 5999;
    final static int BeginPortInterval = 6000;
    final static int EndPortInterval = 6500;

    //Files
    final static String EnvironmentFile = ".\\res\\environment\\last.log";
    static String UsersFile = ".\\res\\users.txt";

    final static boolean LogEnv = false;

    public static void main(String[] args) {
        localizationGUI();
        new Environment(EnvironmentPort);
        new Client("Alica", 6001);
        new Client("Bob", 6007);
    }

    private static void localizationGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        UIManager.put("OptionPane.yesButtonText", "Да");
        UIManager.put("OptionPane.noButtonText", "Нет");
        UIManager.put("OptionPane.cancelButtonText", "Отмена");
        UIManager.put("OptionPane.okButtonText", "Готово");

        FontUIResource f = new FontUIResource(new Font("Verdana", Font.PLAIN, 12));
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object val = UIManager.get(key);
            if (val instanceof FontUIResource) {
                FontUIResource orig = (FontUIResource) val;
                Font font = new Font(f.getFontName(), orig.getStyle(), f.getSize());
                UIManager.put(key, new FontUIResource(font));
            }
        }
    }

    private static Long[] simpleGen(int limitNumber) {
        Long[] simpleNumbers = new Long[limitNumber];
        ArrayList<Long> sns = new ArrayList<>();

        for (int i = 2; i * i < limitNumber; i++) {
            if (simpleNumbers[i] == null) {
                for (int j = i * i; j < limitNumber; j += i) {
                    simpleNumbers[j] = Integer.toUnsignedLong(0);
                }
            }
        }
        for (int i = 2; i < simpleNumbers.length; i++) {
            if (simpleNumbers[i] == null) {
                sns.add(Integer.toUnsignedLong(i));
            }
        }
        return sns.toArray(new Long[0]);
    }
}

class StartFrame extends Frame {

    StartFrame() {
        super("Создание личности");
        createGUI();
    }

    private void createGUI() {
        JPanel mainPanel = new JPanel();

        mainPanel.setLayout(new BorderLayout());

        JTextField name = new JTextField();
        name.setBorder(new TitledBorder("Введите имя:"));
        mainPanel.add(BorderLayout.NORTH, name);
        JTextField port = new JTextField();
        port.setBorder(new TitledBorder("Введите порт (" + Main.BeginPortInterval + "-" + Main.EndPortInterval + "):"));
        mainPanel.add(BorderLayout.SOUTH, port);

        JButton init = new JButton("Подтвердить");
        init.setOpaque(true);
        init.setBackground(new Color(255, 0, 0));
        mainPanel.add(BorderLayout.CENTER, init);
        init.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nameS = name.getText();
                String portS = port.getText();

                if (nameS == null || portS == null || nameS.isEmpty() || !portS.matches("([0-9]){4}") || !nameS.matches("([a-z]|[A-Z]|[0-9])+") || portS.isEmpty()) {
                    JOptionPane.showMessageDialog(StartFrame.this,
                            new String[]{"Не введен порт или имя пользователя"},
                            "Ошибка ввода",
                            JOptionPane.ERROR_MESSAGE);
                } else if (Integer.parseInt(portS) < Main.BeginPortInterval || Integer.parseInt(portS) > Main.EndPortInterval) {
                    JOptionPane.showMessageDialog(StartFrame.this,
                            new String[]{"Порт не принадлежит указанному интервалу"},
                            "Ошибка ввода",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    Main.UsersFile = ".\\res\\" +nameS + ".users";
                    new Client(nameS, Integer.parseInt(portS));
                    dispose();
                }
            }
        });

        add(mainPanel);
        setVisible(true);
        setPreferredSize(new Dimension(300, 300));
        pack();
    }
}