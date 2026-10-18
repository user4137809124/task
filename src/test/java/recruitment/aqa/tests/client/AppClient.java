package recruitment.aqa.tests.client;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import recruitment.aqa.tests.config.TestConfig;

public class AppClient {

    public static final String ENDPOINT = "/endpoint";
    public static final String API_KEY_HEADER = "X-Api-Key";

    private final String apiKey;

    public AppClient() {
        this(TestConfig.API_KEY);
    }

    private AppClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public static AppClient withApiKey(String apiKey) {
        return new AppClient(apiKey);
    }

    @Step("LOGIN, токен {token}")
    public Response login(String token) {
        return post(token, "LOGIN");
    }

    @Step("ACTION, токен {token}")
    public Response action(String token) {
        return post(token, "ACTION");
    }

    @Step("LOGOUT, токен {token}")
    public Response logout(String token) {
        return post(token, "LOGOUT");
    }

    public Response send(String token, String action) {
        String name = "Запрос в приложение: "
                + (token == null ? "без токена" : "токен " + token) + ", "
                + (action == null ? "без действия" : action.isEmpty() ? "пустое действие" : "действие " + action);
        return Allure.step(name, () -> post(token, action));
    }

    private Response post(String token, String action) {
        RequestSpecification spec = baseRequest();
        if (token != null) {
            spec.formParam("token", token);
        }
        if (action != null) {
            spec.formParam("action", action);
        }
        return spec.post(ENDPOINT);
    }

    public RequestSpecification baseRequest() {
        RequestSpecification spec = RestAssured.given()
                .baseUri(TestConfig.APP_URL)
                .filter(new AllureRestAssured()
                        .setRequestAttachmentName("Запрос")
                        .setResponseAttachmentName("Ответ"))
                .contentType(ContentType.URLENC.withCharset("UTF-8"))
                .accept("application/json");
        if (apiKey != null) {
            spec.header(API_KEY_HEADER, apiKey);
        }
        return spec;
    }
}
