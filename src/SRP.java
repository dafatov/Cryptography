import java.util.ArrayList;
import java.util.Random;

abstract class SRP {
    private final Long N = 2 * Main.SimpleNumbers[Main.SimpleNumbers.length - 1] + 1,
            OptionMultiplier = 3L,
            Generator = 2L;
    private Long A,
            B,
            u,
            K,
            M,
            R;

    Long getOptionMultiplier() {
        return OptionMultiplier;
    }

    Long getN() {
        return N;
    }

    Long getK() {
        return K;
    }

    void setK(Long K) {
        this.K = K;
    }

    Long getM() {
        return M;
    }

    Long getR() {
        return R;
    }

    Long getGenerator() {
        return Generator;
    }

    Long getA() {
        return A;
    }

    void setA(Long A) {
        this.A = A;
    }

    Long getB() {
        return B;
    }

    void setB(Long b) {
        B = b;
    }

    Long getU() {
        return u;
    }

    Long pow(Long a, Long b, Long module) {
        System.err.println(b);
        Long r = a;
        for (int i = 0; i < b - 1; i++) {
            r = (r * a) % module;
        }
        return r;
    }

    Long hashFunc(String... args) {
        long res = 0;
        for (String arg : args) {
            for (char string : arg.toCharArray()) {
                res += string;
            }
        }
        return res;
    }

    void calculateU() {
        u = hashFunc(String.valueOf(A), String.valueOf(B));
        System.out.println("u:" + u);
    }

    void calculateM(String login, String salt) {
        M = hashFunc(String.valueOf(hashFunc(String.valueOf(N))^hashFunc(String.valueOf(Generator))),
                String.valueOf(hashFunc(String.valueOf(login))), salt, String.valueOf(A),
                String.valueOf(B), String.valueOf(K));
    }

    void calculateR() {
        R = hashFunc(String.valueOf(A), String.valueOf(M), String.valueOf(K));
    }
}

class clientSRP extends SRP {
    private String salt,
            password;
    private Long v,
            x,
            a;

    clientSRP(boolean isLogin, String password) {
        this.password = password;

        if (isLogin) login();
        else registration();
    }

    String getSalt() {
        return salt;
    }

    void setSalt(String salt) {
        this.salt = salt;
    }

    Long getV() {
        return v;
    }

    private void registration() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            stringBuilder.append((char) new Random().nextInt(Character.MAX_VALUE));
        }
        salt = stringBuilder.toString();

        x = hashFunc(salt, password) % getN();
        v = pow(getGenerator(), x, getN());
    }

    private void login() {
        a = Long.parseLong(String.valueOf(new Random().nextInt(Math.toIntExact(getN()))));
        setA(pow(getGenerator(), a, getN()));
    }

    void calculateK() {
        x = hashFunc(salt, password) % getN();
        setK(hashFunc(String.valueOf(pow(getB() - getOptionMultiplier() * pow(getGenerator(), x, getN()), a + getU() * x, getN()))));
    }

    void calculateM(String login) {
        super.calculateM(login, salt);
    }
}

class serverSRP extends SRP {
    private TripleHashMap<String, String, Long> usersList;//I,s,v
    private Long b;

    serverSRP() {
        usersList = new TripleHashMap<>();
        if (loadUsersList()) System.out.println("Данные загружены");
        else System.out.println("Ошибка в загрузке данных");
    }

    TripleHashMap<String, String, Long> getUsersList() {
        return usersList;
    }

    private boolean loadUsersList() {
        if (!FileManager.exist(Main.UsersFile)) return false;
        ArrayList<String> users = FileManager.read(Main.UsersFile);

        if (users != null) {
            for (String user1 : users) {
                String[] user = user1.split(":");

                usersList.put(user[0], user[1], Long.parseLong(user[2]));
            }
            /*if (usersList.toString().hashCode() != Integer.parseInt(users.get(users.size()-1))) {
                usersList.clear();
                return false;
            }*/
        } else return false;
        return true;
    }

    void calculateB(String login) {
        b = Long.parseLong(String.valueOf(new Random().nextInt(Math.toIntExact(getN()))));
        setB((getOptionMultiplier() * usersList.get(login).get2Arg(0) + pow(getGenerator(), b, getN())) /*% getN()*/);
    }

    void calculateK(String login) {
        setK(hashFunc(String.valueOf(pow(getA() * pow(usersList.get(login).get2Arg(0), getU(), getN()), b, getN()))));
    }

    void calculateM(String login) {
        super.calculateM(login, usersList.get(login).get1Arg(0));
    }

    void saveUsersList() {
        FileManager.erase(Main.UsersFile);
        String status = usersList.toString();
        FileManager.write(Main.UsersFile, status /*+ "\n" + status.hashCode()*/);
    }
}