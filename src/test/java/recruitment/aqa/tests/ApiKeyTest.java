package recruitment.aqa.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import recruitment.aqa.tests.client.AppClient;
import recruitment.aqa.tests.config.TestConfig;
import recruitment.aqa.tests.data.Tokens;
import recruitment.aqa.tests.mock.ExternalServiceMock;
import recruitment.aqa.tests.steps.Checks;
import recruitment.aqa.tests.steps.ExternalChecks;

@Feature("Доступ по API-ключу")
@DisplayName("Доступ по API-ключу")
class ApiKeyTest extends BaseTest {

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Без ключа доступ запрещен")
    void missingKeyIsRejected() {
        String token = Tokens.valid();
        externalService.authResponds(token, 200);

        Checks.error(AppClient.withApiKey(null).login(token), 401);
        ExternalChecks.notCalled(externalService, ExternalServiceMock.AUTH, token);
    }

    @ParameterizedTest(name = "(ключ {0})")
    @ValueSource(strings = {"wrongKey", "QAZWSXEDC", "qazWSXed"})
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("С неверным ключом доступ запрещен")
    void wrongKeyIsRejected(String key) {
        String token = Tokens.valid();
        externalService.authResponds(token, 200);

        Checks.error(AppClient.withApiKey(key).login(token), 401);
        ExternalChecks.notCalled(externalService, ExternalServiceMock.AUTH, token);
    }

    @Test
    @DisplayName("С пустым ключом доступ запрещен")
    void emptyKeyIsRejected() {
        String token = Tokens.valid();
        externalService.authResponds(token, 200);

        Checks.error(AppClient.withApiKey("").login(token), 401);
        ExternalChecks.notCalled(externalService, ExternalServiceMock.AUTH, token);
    }

    @Test
    @DisplayName("Без ключа нельзя завершить чужую сессию")
    @Description("Если знать чужой токен, но не знать ключ, разлогинить пользователя не получится.")
    void logoutWithoutKeyDoesNotEndSession() {
        String token = loggedInUser();

        Checks.error(AppClient.withApiKey("wrongKey").logout(token), 401);

        externalService.doActionResponds(token, 200);
        Checks.success(app.action(token));
    }

    @Test
    @DisplayName("Ключ доступа не передается во внешний сервис")
    void apiKeyIsNotSentToExternalService() {
        String token = loggedInUser();

        var request = ExternalChecks.called(externalService, ExternalServiceMock.AUTH, token, 1).get(0);
        ExternalChecks.apiKeyNotLeaked(request, TestConfig.API_KEY);
    }
}
