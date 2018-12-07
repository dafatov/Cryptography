import java.util.Random;

class DiffieHellman {
    private Long publicKey = Integer.toUnsignedLong(0),
            sharedPublicKey = Integer.toUnsignedLong(0),
            P,
            A,
            secretKey = Integer.toUnsignedLong(0),
            sharedSecretKey = Integer.toUnsignedLong(0);

    Long getP() {
        return P;
    }

    Long getA() {
        return A;
    }

    void setSharedPublicKey(Long sharedPublicKey) {
        this.sharedPublicKey = sharedPublicKey;
    }

    Long getPublicKey() {
        return publicKey;
    }

    Long getSharedSecretKey() {
        return sharedSecretKey;
    }

    Long getSecretKey() {
        return secretKey;
    }

    void setP(Long p) {
        P = p;
    }

    void setA(Long a) {
        A = a;
    }

    DiffieHellman() {
        Random random = new Random();
        int loopCount = Main.limitLoops;

        P = Main.SimpleNumbers[random.nextInt(Main.SimpleNumbers.length)];
        while (((A = Integer.toUnsignedLong(random.nextInt(Math.toIntExact(P)))) >= P - 1) || (A <= 1)) {
            if (loopCount == 0) break;
            loopCount--;
        }
    }

    boolean pickSecretKey() {
        if (A==0) return false;
        Random random = new Random();
        int loopCount = Main.limitLoops;

        while (((secretKey = Integer.toUnsignedLong(random.nextInt(Math.toIntExact(P)))) >= P) || (secretKey <= 0)) {
            if (loopCount == 0) {
                return false;
            }
            loopCount--;
        }
        return true;
    }

    void calcPublicKey() {
        publicKey = Long.parseLong(String.valueOf(pow(P, A, secretKey)).split("\\.")[0]);
    }

    void calcSharedSecretKey() {
        sharedSecretKey = Long.parseLong(String.valueOf(pow(P, sharedPublicKey, secretKey)).split("\\.")[0]);
    }

    private Long pow(Long module, Long a, Long b) {
        Long r = a;
        for (int i = 0; i < b - 1; i++) {
            r = (r * a) % module;
        }
        return r;
    }
}
