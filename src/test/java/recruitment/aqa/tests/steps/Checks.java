package recruitment.aqa.tests.steps;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import recruitment.aqa.tests.client.AppResponse;

import static org.assertj.core.api.Assertions.assertThat;

public final class Checks {

    private Checks() {
    }

    @Step("Ждем ответ OK")
    public static void success(Response response) {
        assertThat(response.statusCode())
                .withFailMessage("Запрос должен был пройти, но пришел код %s. Ответ: %s",
                        response.statusCode(), response.asString())
                .isEqualTo(200);
        AppResponse body = parse(response);
        assertThat(body.result())
                .withFailMessage("В result пришло %s вместо OK", body.result())
                .isEqualTo("OK");
        assertThat(body.message())
                .withFailMessage("В успешном ответе есть сообщение об ошибке: %s", body.message())
                .isNull();
    }

    @Step("Ждем отказ")
    public static AppResponse error(Response response) {
        assertThat(response.statusCode())
                .withFailMessage("Приложение должно было отказать, но ответило %s (запрос прошел). Ответ: %s",
                        response.statusCode(), response.asString())
                .isGreaterThanOrEqualTo(400);
        return checkErrorBody(response);
    }

    @Step("Ждем отказ с кодом {expectedStatus}")
    public static AppResponse error(Response response, int expectedStatus) {
        assertThat(response.statusCode())
                .withFailMessage("Ждали код %s, пришел %s. Ответ: %s",
                        expectedStatus, response.statusCode(), response.asString())
                .isEqualTo(expectedStatus);
        return checkErrorBody(response);
    }

    @Step("Ждем, что приложение назовет причину (в тексте ошибки есть \"{text}\")")
    public static void messageContains(AppResponse body, String text) {
        assertThat(body.message())
                .withFailMessage("В тексте ошибки нет \"%s\". Текст: %s", text, body.message())
                .containsIgnoringCase(text);
    }

    private static AppResponse checkErrorBody(Response response) {
        AppResponse body = parse(response);
        assertThat(body.result())
                .withFailMessage("В result пришло %s вместо ERROR", body.result())
                .isEqualTo("ERROR");
        assertThat(body.message())
                .withFailMessage("Приложение не написало причину ошибки (message пустой)")
                .isNotBlank();
        return body;
    }

    private static AppResponse parse(Response response) {
        assertThat(response.contentType())
                .withFailMessage("Ответ пришел не в JSON, Content-Type: %s", response.contentType())
                .startsWith("application/json");
        return response.as(AppResponse.class);
    }
}
