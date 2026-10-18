package recruitment.aqa.tests.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class TestConfig {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = TestConfig.class.getResourceAsStream("/test.properties")) {
            PROPS.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String APP_URL = get("app.url");
    public static final String API_KEY = get("app.api.key");
    public static final int MOCK_PORT = Integer.parseInt(get("mock.port"));

    private TestConfig() {
    }

    private static String get(String key) {
        return System.getProperty(key, PROPS.getProperty(key));
    }
}
