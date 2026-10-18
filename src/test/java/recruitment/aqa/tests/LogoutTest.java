package recruitment.aqa.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import recruitment.aqa.tests.data.Tokens;
import recruitment.aqa.tests.mock.ExternalServiceMock;
import recruitment.aqa.tests.steps.Checks;
import recruitment.aqa.tests.steps.ExternalChecks;

@Feature("Выход из системы (LOGOUT)")
@DisplayName("Выход из системы (LOGOUT)")
class LogoutTest extends BaseTest {

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Вошедший пользователь успешно выходит из системы")
    void logoutAfterLogin() {
        String token = loggedInUser();

        Checks.success(app.logout(token));
    }

    @Test
    @DisplayName("После выхода действия запрещены")
    void actionAfterLogoutIsRejected() {
        String token = loggedInUser();
        Checks.success(app.logout(token));

        externalService.doActionResponds(token, 200);
        Checks.error(app.action(token));
        ExternalChecks.notCalled(externalService, ExternalServiceMock.DO_ACTION, token);
    }

    @Test
    @DisplayName("Выход без предварительного входа отклоняется")
    void logoutWithoutLogin() {
        var body = Checks.error(app.logout(Tokens.valid()));
        Checks.messageContains(body, "not found");
    }

    @Test
    @DisplayName("Повторный выход отклоняется")
    void repeatedLogout() {
        String token = loggedInUser();
        Checks.success(app.logout(token));

        Checks.error(app.logout(token));
    }

    @Test
    @DisplayName("После выхода можно снова войти и работать")
    void loginAgainAfterLogout() {
        String token = loggedInUser();
        Checks.success(app.logout(token));

        Checks.success(app.login(token));
        externalService.doActionResponds(token, 200);
        Checks.success(app.action(token));
    }

    @Test
    @DisplayName("Выход не обращается к внешнему сервису")
    @Description("По заданию LOGOUT просто удаляет токен у себя, во внешний сервис ходить не должен.")
    void logoutDoesNotCallExternalService() {
        String token = loggedInUser();
        Checks.success(app.logout(token));

        ExternalChecks.called(externalService, ExternalServiceMock.AUTH, token, 1);
        ExternalChecks.notCalled(externalService, ExternalServiceMock.DO_ACTION, token);
    }

    @Test
    @DisplayName("Выход одного пользователя не затрагивает другого")
    void logoutDoesNotAffectOtherUser() {
        String first = loggedInUser();
        String second = loggedInUser();

        Checks.success(app.logout(first));

        externalService.doActionResponds(second, 200);
        Checks.success(app.action(second));
    }
}
