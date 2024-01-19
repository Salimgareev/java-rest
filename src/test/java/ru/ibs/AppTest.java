package ru.ibs;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
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
 * Тестирование REST API
 */
public class AppTest {

    @ParameterizedTest
    @CsvSource({
            "йцю123qw!@#$%^&*()_+/|, VEGETABLE, false",
            "Ананас, FRUIT, true"
    })
    public void restTest(String name, String type, boolean exotic) throws JsonProcessingException {
        final String BASE_URL = "http://localhost:8080/api/food";
        final String RESET_URL = "http://localhost:8080/api/data/reset";

        final String requestBody = "{\n" +
                "        \"name\": \"" + name + "\",\n" +
                "        \"type\": \"" + type + "\",\n" +
                "        \"exotic\": " + exotic + "\n" +
                "}";

        RequestSpecification requestSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setAccept("*/*")
                .setContentType(ContentType.JSON)
                .build();

        Response responseGet = given()
                .spec(requestSpec)
                .when()
                .get("/")
                .then()
                .statusCode(200)
                .extract().response();

        responseGet.getBody().prettyPrint();

        // Здесь будут куки от предыдущего запроса
        Cookies cookies = responseGet.getDetailedCookies();

        given()
                .cookies(cookies)
                .spec(requestSpec)
                .body(requestBody)
                .post("/")
                .then()
                .statusCode(200)
                .extract().response();

        Response responseGet2 = given()
                .cookies(cookies)
                .spec(requestSpec)
                .when()
                .get("/");

        String responseBodyPost = responseGet2.getBody().asString();
        System.out.println(" ----------------- List --------------------------- ");

        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> responseList = objectMapper.readValue(responseBodyPost,
                new TypeReference<>() {
                });

        Assertions.assertEquals(5, responseList.size(), "Кол-во элементов не соответствует!");

        responseList.forEach(System.out::println);

        // Получаем последний элемент из списка
        Map<String, Object> lastElement = responseList.get(responseList.size() - 1);

        Assertions.assertEquals(name, lastElement.get("name"),
                "Название элемента не соответствует ожидаемому!");
        Assertions.assertEquals(type, lastElement.get("type"),
                "Тип элемента не соответствует ожидаемому!");
        Assertions.assertEquals(exotic, lastElement.get("exotic"),
                "Экзотичность элемента не соответствует ожидаемой!");

        given()
                .baseUri(RESET_URL)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract().response();
    }
}
