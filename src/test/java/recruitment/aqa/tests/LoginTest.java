package recruitment.aqa.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import recruitment.aqa.tests.data.Tokens;
import recruitment.aqa.tests.mock.ExternalServiceMock;
import recruitment.aqa.tests.steps.Checks;
import recruitment.aqa.tests.steps.ExternalChecks;

@Feature("Вход в систему (LOGIN)")
@DisplayName("Вход в систему (LOGIN)")
class LoginTest extends BaseTest {

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Успешный вход, когда внешний сервис подтверждает пользователя")
    void successfulLogin() {
        String token = Tokens.valid();
        externalService.authResponds(token, 200);

        Checks.success(app.login(token));
    }

    @Test
    @DisplayName("При входе приложение отправляет во внешний сервис корректный запрос /auth")
    void loginSendsAuthRequest() {
        String token = loggedInUser();

        var request = ExternalChecks.called(externalService, ExternalServiceMock.AUTH, token, 1).get(0);
        ExternalChecks.matchesContract(request, token);
        ExternalChecks.notCalled(externalService, ExternalServiceMock.DO_ACTION, token);
    }

    @ParameterizedTest(name = "(код {0})")
    @ValueSource(ints = {400, 401, 500, 503})
    @DisplayName("Вход отклоняется, если внешний сервис отвечает ошибкой")
    void loginFailsWhenAuthFails(int status) {
        String token = Tokens.valid();
        externalService.authResponds(token, status);

        Checks.error(app.login(token));
    }

    @Test
    @DisplayName("После неуспешного входа токен не сохраняется")
    void tokenIsNotStoredAfterFailedLogin() {
        String token = Tokens.valid();
        externalService.authResponds(token, 500);
        Checks.error(app.login(token));

        externalService.doActionResponds(token, 200);
        Checks.error(app.action(token));
        ExternalChecks.notCalled(externalService, ExternalServiceMock.DO_ACTION, token);
    }

    @Test
    @DisplayName("После неуспешного входа можно войти повторно")
    void loginRetryAfterFailure() {
        String token = Tokens.valid();
        externalService.authResponds(token, 503);
        Checks.error(app.login(token));

        externalService.authResponds(token, 200);
        Checks.success(app.login(token));
    }

    @Test
    @DisplayName("Повторный вход с уже активным токеном отклоняется")
    @Description("Токен уже залогинен, второй LOGIN получает ошибку. Первая сессия при этом не ломается.")
    void repeatedLoginIsRejected() {
        String token = loggedInUser();

        var body = Checks.error(app.login(token), 409);
        Checks.messageContains(body, "already exists");

        externalService.doActionResponds(token, 200);
        Checks.success(app.action(token));
    }

    @Test
    @DisplayName("Вход отклоняется, если внешний сервис отвечает перенаправлением (302)")
    @Description("""
            По заданию токен сохраняется только "в случае успеха" проверки на внешнем сервисе. \
            Код 302 означает "ответ находится по другому адресу", а не "пользователь подтвержден". \
            Сейчас приложение считает 302 успехом и пускает пользователя, хотя внешний сервис его не подтвердил.""")
    void loginFailsOnRedirect() {
        String token = Tokens.valid();
        externalService.authResponds(token, 302);

        Checks.error(app.login(token));
    }

    @Test
    @DisplayName("Вход отклоняется, если внешний сервис долго не отвечает")
    @Description("Если внешний сервис завис, пользователя пускать нельзя.")
    void loginFailsOnExternalTimeout() {
        String token = Tokens.valid();
        externalService.stubWithDelay(ExternalServiceMock.AUTH, token, 12);

        Checks.error(app.login(token));

        externalService.doActionResponds(token, 200);
        Checks.error(app.action(token));
    }
}
