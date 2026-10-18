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

@Feature("Действие пользователя (ACTION)")
@DisplayName("Действие пользователя (ACTION)")
class ActionTest extends BaseTest {

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Вошедший пользователь успешно выполняет действие")
    void actionAfterLogin() {
        String token = loggedInUser();
        externalService.doActionResponds(token, 200);

        Checks.success(app.action(token));

        var request = ExternalChecks.called(externalService, ExternalServiceMock.DO_ACTION, token, 1).get(0);
        ExternalChecks.matchesContract(request, token);
    }

    @Test
    @DisplayName("Пользователь может выполнять действия многократно")
    void severalActions() {
        String token = loggedInUser();
        externalService.doActionResponds(token, 200);

        Checks.success(app.action(token));
        Checks.success(app.action(token));
        Checks.success(app.action(token));

        ExternalChecks.called(externalService, ExternalServiceMock.DO_ACTION, token, 3);
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Без входа действие запрещено")
    void actionWithoutLogin() {
        String token = Tokens.valid();
        externalService.doActionResponds(token, 200);

        var body = Checks.error(app.action(token));
        Checks.messageContains(body, "not found");
        ExternalChecks.notCalled(externalService, ExternalServiceMock.DO_ACTION, token);
    }

    @Test
    @DisplayName("Вход одного пользователя не дает права действовать другому")
    void loginOfOtherUserDoesNotGrantAccess() {
        loggedInUser();
        String otherToken = Tokens.valid();
        externalService.doActionResponds(otherToken, 200);

        Checks.error(app.action(otherToken));
        ExternalChecks.notCalled(externalService, ExternalServiceMock.DO_ACTION, otherToken);
    }

    @ParameterizedTest(name = "(код {0})")
    @ValueSource(ints = {400, 500, 503})
    @DisplayName("Действие завершается ошибкой, если внешний сервис отвечает ошибкой")
    void actionFailsWhenExternalFails(int status) {
        String token = loggedInUser();
        externalService.doActionResponds(token, status);

        Checks.error(app.action(token));
    }

    @Test
    @DisplayName("Ошибка действия не завершает сессию пользователя")
    void sessionSurvivesFailedAction() {
        String token = loggedInUser();
        externalService.doActionResponds(token, 500);
        Checks.error(app.action(token));

        externalService.doActionResponds(token, 200);
        Checks.success(app.action(token));
    }

    @Test
    @DisplayName("Действие завершается ошибкой, если внешний сервис отвечает перенаправлением (302)")
    @Description("""
            Код 302 не означает, что действие выполнено. Сейчас приложение отвечает пользователю OK, \
            хотя внешний сервис действие не подтвердил.""")
    void actionFailsOnRedirect() {
        String token = loggedInUser();
        externalService.doActionResponds(token, 302);

        Checks.error(app.action(token));
    }
}
