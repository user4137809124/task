package recruitment.aqa.tests.data;

import java.util.concurrent.ThreadLocalRandom;

public final class Tokens {

    public static final int LENGTH = 32;
    public static final String LETTERS_G_TO_Z = "GHIJKLMNOPQRSTUVWXYZ";

    private static final String HEX = "0123456789ABCDEF";

    private Tokens() {
    }

    // приложение по факту пропускает только 0-9A-F (см. README, дефект 1),
    // поэтому для сценариев берем токены из пересечения с A-Z0-9
    public static String valid() {
        return random(HEX, LENGTH);
    }

    public static String validWithLettersGtoZ() {
        return random(LENGTH - LETTERS_G_TO_Z.length()) + LETTERS_G_TO_Z;
    }

    public static String withChar(char c) {
        return random(LENGTH - 1) + c;
    }

    public static String random(int length) {
        return random(HEX, length);
    }

    public static String random(String alphabet, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(ThreadLocalRandom.current().nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}
