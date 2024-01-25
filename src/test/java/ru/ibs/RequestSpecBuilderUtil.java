package ru.ibs;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Класс для управления RequestSpecification
 */
public class RequestSpecBuilderUtil {
    /**
     * Создает и возвращает объект RequestSpecification
     *
     * @return RequestSpecification объект спецификации для дальнейшего создания запросов
     */
    public static RequestSpecification buildRequestSpec(String baseUri) {
        return new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setAccept("*/*")
                .setContentType(ContentType.JSON)
                .build();
    }
}
