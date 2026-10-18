package recruitment.aqa.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import recruitment.aqa.tests.client.AppClient;
import recruitment.aqa.tests.data.Tokens;
import recruitment.aqa.tests.steps.Checks;

@Feature("Некорректные HTTP-запросы")
@DisplayName("Некорректные HTTP-запросы")
class ProtocolTest extends BaseTest {

    @Test
    @DisplayName("Запрос методом GET отклоняется как неподдерживаемый (405)")
    @Description("""
            Приложение принимает только POST-запросы. На GET оно должно отвечать 405 "метод не поддерживается". \
            Сейчас отвечает 500 "внутренняя ошибка сервера", и по такому ответу нельзя понять, \
            что ошибся клиент, а не сломался сервер.""")
    void getMethodIsNotAllowed() {
        Checks.error(sendGet(), 405);
    }

    @Test
    @DisplayName("Запрос на несуществующий адрес возвращает 404")
    @Description("""
            У приложения только один адрес /endpoint. На любой другой адрес оно должно отвечать \
            "не найдено" (404). Сейчас оно отвечает 500, как будто у него внутренний сбой.""")
    void unknownPathReturnsNotFound() {
        Checks.error(sendToPath("/unknown"), 404);
    }

    @Test
    @DisplayName("Запрос в формате JSON отклоняется как неподдерживаемый (415)")
    @Description("""
            По заданию приложение принимает данные только в формате формы (x-www-form-urlencoded). \
            На другой формат оно должно отвечать 415 "формат не поддерживается", а сейчас отвечает 500.""")
    void jsonBodyIsUnsupported() {
        Checks.error(sendJson(), 415);
    }

    @Step("Отправить GET-запрос на /endpoint")
    private Response sendGet() {
        return app.baseRequest()
                .queryParam("token", Tokens.valid())
                .queryParam("action", "LOGOUT")
                .get(AppClient.ENDPOINT);
    }

    @Step("Отправить POST-запрос на {path}")
    private Response sendToPath(String path) {
        return app.baseRequest()
                .formParam("token", Tokens.valid())
                .formParam("action", "LOGOUT")
                .post(path);
    }

    @Step("Отправить POST-запрос с телом в формате JSON")
    private Response sendJson() {
        return app.baseRequest()
                .contentType(ContentType.JSON)
                .body("{\"token\":\"" + Tokens.valid() + "\",\"action\":\"LOGOUT\"}")
                .post(AppClient.ENDPOINT);
    }
}
