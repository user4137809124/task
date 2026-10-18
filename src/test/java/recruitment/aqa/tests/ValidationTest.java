package recruitment.aqa.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import recruitment.aqa.tests.data.Tokens;
import recruitment.aqa.tests.mock.ExternalServiceMock;
import recruitment.aqa.tests.steps.Checks;
import recruitment.aqa.tests.steps.ExternalChecks;

import java.util.stream.Stream;

@Feature("Проверка входных данных")
@DisplayName("Проверка входных данных")
class ValidationTest extends BaseTest {

    static Stream<Arguments> invalidTokens() {
        return Stream.of(
                Arguments.of("пустая строка", ""),
                Arguments.of("1 символ", Tokens.random(1)),
                Arguments.of("31 символ", Tokens.random(31)),
                Arguments.of("33 символа", Tokens.random(33)),
                Arguments.of("строчная буква", Tokens.withChar('a')),
                Arguments.of("спецсимвол", Tokens.withChar('-')),
                Arguments.of("пробел внутри", Tokens.withChar(' ')),
                Arguments.of("кириллица", Tokens.withChar('Ж')));
    }

    @ParameterizedTest(name = "({0})")
    @MethodSource("invalidTokens")
    @DisplayName("Некорректный токен отклоняется")
    void invalidTokenIsRejected(String caseName, String token) {
        externalService.authResponds(token, 200);

        var body = Checks.error(app.login(token), 400);
        Checks.messageContains(body, "token");
        ExternalChecks.notCalled(externalService, ExternalServiceMock.AUTH, token);
    }

    @Test
    @DisplayName("Запрос без токена отклоняется")
    void missingTokenIsRejected() {
        var body = Checks.error(app.send(null, "LOGIN"), 400);
        Checks.messageContains(body, "token");
    }

    @Test
    @DisplayName("Токен с буквами G-Z принимается")
    @Description("""
            По заданию токен состоит из 32 символов A-Z и 0-9. Приложение же принимает только цифры \
            и буквы A-F, а токен с любой другой буквой (G, K, Z...) отклоняет как неверный. \
            Если настоящие токены генерируются из A-Z0-9, почти в каждом будет буква дальше F, \
            и войти не сможет почти никто.""")
    void tokenWithAnyLatinLetterIsAccepted() {
        String token = Tokens.validWithLettersGtoZ();
        externalService.authResponds(token, 200);

        Checks.success(app.login(token));
    }

    @Test
    @DisplayName("Токен только из цифр принимается")
    void digitsOnlyTokenIsAccepted() {
        String token = Tokens.random("0123456789", Tokens.LENGTH);
        externalService.authResponds(token, 200);

        Checks.success(app.login(token));
    }

    @ParameterizedTest(name = "({0})")
    @ValueSource(strings = {"DELETE", "login", "LOG IN", "ACTIONS"})
    @DisplayName("Неизвестное действие отклоняется")
    void unknownActionIsRejected(String action) {
        String token = Tokens.valid();
        externalService.authResponds(token, 200);

        var body = Checks.error(app.send(token, action), 400);
        Checks.messageContains(body, "action");
        ExternalChecks.notCalled(externalService, ExternalServiceMock.AUTH, token);
    }

    @Test
    @DisplayName("Пустое действие отклоняется")
    void emptyActionIsRejected() {
        var body = Checks.error(app.send(Tokens.valid(), ""), 400);
        Checks.messageContains(body, "action");
    }

    @Test
    @DisplayName("Запрос без действия отклоняется")
    void missingActionIsRejected() {
        var body = Checks.error(app.send(Tokens.valid(), null), 400);
        Checks.messageContains(body, "action");
    }

    @Test
    @DisplayName("Пустой запрос отклоняется с описанием обеих проблем")
    void emptyRequestReportsAllProblems() {
        var body = Checks.error(app.send(null, null), 400);
        Checks.messageContains(body, "token");
        Checks.messageContains(body, "action");
    }
}
