package recruitment.aqa.tests.mock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class ExternalServiceMock {

    public static final String AUTH = "/auth";
    public static final String DO_ACTION = "/doAction";

    private final WireMockServer server;

    public ExternalServiceMock(int port) {
        this.server = new WireMockServer(options().port(port));
    }

    public void start() {
        server.start();
        // пока тест явно не настроил ответ, сервис отвечает ошибкой
        server.stubFor(post(urlPathEqualTo(AUTH)).atPriority(10)
                .willReturn(aResponse().withStatus(500).withBody("{\"error\":\"not configured\"}")));
        server.stubFor(post(urlPathEqualTo(DO_ACTION)).atPriority(10)
                .willReturn(aResponse().withStatus(500).withBody("{\"error\":\"not configured\"}")));
    }

    @Step("Внешний сервис на /auth отвечает кодом {status}")
    public void authResponds(String token, int status) {
        stub(AUTH, token, status, "{}");
    }

    @Step("Внешний сервис на /doAction отвечает кодом {status}")
    public void doActionResponds(String token, int status) {
        stub(DO_ACTION, token, status, "{}");
    }

    private void stub(String path, String token, int status, String body) {
        server.stubFor(post(urlPathEqualTo(path)).atPriority(1)
                .withFormParam("token", equalTo(token))
                .willReturn(aResponse().withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    @Step("Внешний сервис на {path} отвечает с задержкой {seconds} с")
    public void stubWithDelay(String path, String token, int seconds) {
        server.stubFor(post(urlPathEqualTo(path)).atPriority(1)
                .withFormParam("token", equalTo(token))
                .willReturn(aResponse().withStatus(200).withBody("{}").withFixedDelay(seconds * 1000)));
    }

    public List<LoggedRequest> requests(String path, String token) {
        List<LoggedRequest> requests = server.findAll(postRequestedFor(urlPathEqualTo(path))
                .withFormParam("token", equalTo(token)));
        Allure.addAttachment("Что пришло на " + path, "text/plain",
                requests.isEmpty() ? "Запросов не было" : requests.toString());
        return requests;
    }
}
