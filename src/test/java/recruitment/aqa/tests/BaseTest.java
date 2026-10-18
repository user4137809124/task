package recruitment.aqa.tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Step;
import org.junit.jupiter.api.BeforeAll;
import recruitment.aqa.tests.client.AppClient;
import recruitment.aqa.tests.config.TestConfig;
import recruitment.aqa.tests.data.Tokens;
import recruitment.aqa.tests.mock.ExternalServiceMock;
import recruitment.aqa.tests.steps.Checks;

@Epic("Сервис действий пользователя")
public abstract class BaseTest {

    protected static ExternalServiceMock externalService;
    protected final AppClient app = new AppClient();

    @BeforeAll
    static void startExternalService() {
        if (externalService == null) {
            externalService = new ExternalServiceMock(TestConfig.MOCK_PORT);
            externalService.start();
        }
    }

    @Step("Пользователь вошел в систему")
    protected void loggedIn(String token) {
        externalService.authResponds(token, 200);
        Checks.success(app.login(token));
    }

    protected String loggedInUser() {
        String token = Tokens.valid();
        loggedIn(token);
        return token;
    }
}
