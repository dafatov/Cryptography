class Coder {
    private static final char COUNTOFSIMBOLS = Character.MAX_VALUE - Character.MIN_VALUE;

    static String code(Long key, String message) {
        char tmp = (char) (key % (COUNTOFSIMBOLS));
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);


            c += tmp;
            c %= COUNTOFSIMBOLS + 1;
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    static String decode(Long key, String message) {
        char tmp = (char) (key % (COUNTOFSIMBOLS));
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);

            c = (char) (c < tmp ? COUNTOFSIMBOLS - tmp + c + 1 : c - tmp);
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    static String codeRSA(Long e, Long n, String message) {
        char[] string = message.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        long res;

        for (int i = 0; i < string.length; i++) {
            res = string[i];
            for (int j = 0; j < e - 1; j++) {
                res = ((res % n) * (string[i] % n)) % n;
            }
            stringBuilder.append(res);
            if (i != string.length - 1) stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    static String decodeRSA(Long d, Long n, String message)  {
        String[] symbols = message.split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        long res;

        for (String symbol : symbols) {
            res = Long.parseLong(symbol);
            for (int j = 0; j < d - 1; j++) {
                res = ((res % n) * (Long.parseLong(symbol) % n)) % n;
            }
            stringBuilder.append((char) res);
        }
        return stringBuilder.toString();
    }
}
