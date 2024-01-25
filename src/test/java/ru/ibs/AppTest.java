package ru.ibs;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Класс для тестирования REST API
 */
public class AppTest {
    /**
     * Базовый URL для выполнения запросов
     */
    private static final String BASE_URL = "http://localhost:8080/api/food";

    /**
     * URL для сброса данных
     */
    private static final String RESET_URL = "http://localhost:8080/api/data/reset";

    /**
     * Параметризированный тест для выполнения запросов и проверки результатов
     *
     * @param name   Название продукта
     * @param type   Тип продукта
     * @param exotic Флаг экзотичности продукта
     */
    @ParameterizedTest
    @CsvSource({
            "йцю123qw!@#$%^&*()_+/|, VEGETABLE, false",
            "Ананас, FRUIT, true"
    })
    public void restTest(String name, String type, boolean exotic) throws JsonProcessingException {
        final RequestSpecification requestSpec = RequestSpecBuilderUtil.buildRequestSpec(BASE_URL);

        // Выполняем GET-запрос для просмотра данных
        Response responseGet = executeGetRequest(requestSpec);

        // Вывести товары в консоль в удобном представлении
        responseGet.getBody().prettyPrint();

        // Преобразование тела ответа в список Map
        List<Map<String, Object>> responseList1 = responseGet.jsonPath().getList("$");

        // Получение количества объектов
        int numberOfObjectsInFirstGet = responseList1.size();

        // Здесь будут куки от предыдущего запроса
        Cookies cookies = responseGet.getDetailedCookies();

        // Выполняем POST-запрос
        executePostRequestWithCookies(requestSpec, name, type, exotic, cookies);

        // Выполняем GET-запрос после добавления элемента
        Response responseGet2 = executeGetRequestWithCookies(requestSpec, cookies);

        // Проверка данных после добавления элемента
        CheckDataAfterPost(responseGet2, numberOfObjectsInFirstGet, name, type, exotic);

        // Сброс данных
        resetData();
    }

    private void CheckDataAfterPost(Response responseGet, int numberOfObjectsInFirstGet,
                                    String name, String type, boolean exotic) throws JsonProcessingException {
        String responseBodyPost = responseGet.getBody().asString();

        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> responseList = objectMapper.readValue(responseBodyPost, new TypeReference<>() {});

        // Проверка, что после добавления товара их количество стало на 1 больше
        Assertions.assertEquals(numberOfObjectsInFirstGet + 1, responseList.size(),
                "Кол-во элементов не соответствует ожидаемому значению!");

        responseList.forEach(System.out::println);

        // Получаем последний элемент из списка
        Map<String, Object> lastElement = responseList.get(responseList.size() - 1);

        // Проверка значений в товаре
        Assertions.assertEquals(name, lastElement.get("name"),
                "Название элемента не соответствует ожидаемому!");
        Assertions.assertEquals(type, lastElement.get("type"),
                "Тип элемента не соответствует ожидаемому!");
        Assertions.assertEquals(exotic, lastElement.get("exotic"),
                "Экзотичность элемента не соответствует ожидаемой!");
    }

    /**
     * Выполняет GET-запрос с Cookies
     *
     * @param requestSpec Спецификация для get-запроса
     * @param cookies Куки от предыдущего запроса
     * */
    private Response executeGetRequestWithCookies(RequestSpecification requestSpec, Cookies cookies) {
        return given().cookies(cookies).spec(requestSpec).when().get("/").then().statusCode(200).extract().response();
    }

    /**
     * Выполняет GET-запрос
     *
     * @param requestSpec Спецификация для get-запроса
     * */
    private Response executeGetRequest(RequestSpecification requestSpec) {
        return given().spec(requestSpec).when().get("/").then().statusCode(200).extract().response();
    }

    /**
     * Выполняет POST-запрос для добавления элемента
     *
     * @param requestSpec RequestSpecification объект спецификации
     * @param name        Название продукта
     * @param type        Тип продукта
     * @param exotic      Флаг экзотичности продукта
     * @param cookies     Куки для понимания того, кто отправил запрос
     */
    private void executePostRequestWithCookies(RequestSpecification requestSpec, String name, String type,
                                    boolean exotic, Cookies cookies) {
        given().cookies(cookies).spec(requestSpec).body(buildRequestBody(name, type, exotic)).
                post("/").then().statusCode(200);
    }

    /**
     * Строит тело для POST-запроса
     *
     * @param name   Название продукта
     * @param type   Тип продукта
     * @param exotic Флаг экзотичности продукта
     * @return Тело запроса в виде строки
     */
    private String buildRequestBody(String name, String type, boolean exotic) {
        return String.format("{\n\"name\": \"%s\",\n\"type\": \"%s\",\n\"exotic\": %b\n}", name, type, exotic);
    }

    /**
     * Выполняет сброс данных
     */
    private void resetData() {
        given().baseUri(RESET_URL).when().post().then().statusCode(200);
    }
}