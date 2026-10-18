package recruitment.aqa.tests.steps;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import recruitment.aqa.tests.mock.ExternalServiceMock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class ExternalChecks {

    private ExternalChecks() {
    }

    public static List<LoggedRequest> called(ExternalServiceMock mock, String path, String token, int times) {
        return Allure.step("Ждем запросов на " + path + ": " + times, () -> {
            List<LoggedRequest> requests = mock.requests(path, token);
            assertThat(requests)
                    .withFailMessage("На %s должно было прийти запросов: %s, а пришло: %s",
                            path, times, requests.size())
                    .hasSize(times);
            return requests;
        });
    }

    @Step("Ждем, что приложение не ходило на {path}")
    public static void notCalled(ExternalServiceMock mock, String path, String token) {
        int count = mock.requests(path, token).size();
        assertThat(count)
                .withFailMessage("Приложение не должно было ходить на %s, а сходило %s раз(а)", path, count)
                .isZero();
    }

    @Step("Ждем, что запрос во внешний сервис оформлен как в задании")
    public static void matchesContract(LoggedRequest request, String token) {
        assertThat(request.getMethod().getName()).as("метод").isEqualTo("POST");
        assertThat(request.getHeader("Content-Type")).as("Content-Type")
                .startsWith("application/x-www-form-urlencoded");
        assertThat(request.getHeader("Accept")).as("Accept").isEqualTo("application/json");
        assertThat(request.getBodyAsString()).as("тело запроса").isEqualTo("token=" + token);
    }

    @Step("Ждем, что API-ключ не ушел во внешний сервис")
    public static void apiKeyNotLeaked(LoggedRequest request, String apiKey) {
        assertThat(request.containsHeader("X-Api-Key"))
                .withFailMessage("Во внешний сервис ушел заголовок X-Api-Key")
                .isFalse();
        assertThat(request.getBodyAsString())
                .withFailMessage("Ключ попал в тело запроса к внешнему сервису: %s", request.getBodyAsString())
                .doesNotContain(apiKey);
    }
}
