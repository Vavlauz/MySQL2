package ru.netology.domain;

import org.junit.jupiter.api.Test;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import lombok.val;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;


import java.sql.DriverManager;
import java.sql.SQLException;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestApi {

    private static RequestSpecification requestSpec = new RequestSpecBuilder()
            .setBaseUri("http://localhost")
            .setPort(9999)
            .setAccept(ContentType.JSON)
            .setContentType(ContentType.JSON)
            .log(LogDetail.ALL)
            .build();
    int balance1;
    int balance2;
    int sum = 5000;

    @Test
    void shouldTransfer() throws SQLException {
        // сам запрос
        given() // "дано"
                .spec(requestSpec) // указываем, какую спецификацию используем
                .body(DataHelper.getAuthInfo()) // передаём в теле объект, который будет преобразован в JSON
                .when() // "когда"
                .post("/api/auth") // на какой путь, относительно BaseUri отправляем запрос
                .then() // "тогда ожидаем"
                .statusCode(200); // код 200 OK

        val codeSQL = "SELECT code FROM auth_codes WHERE created = (SELECT max(created) FROM auth_codes);";
        val runner = new QueryRunner();

        try (
                val conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/app", "user", "pass"
                );
        ) {
            val code = runner.query(conn, codeSQL, new ScalarHandler<>());
            System.out.println(code);

            String token =
                    given() // "дано"
                            .spec(requestSpec) // указываем, какую спецификацию используем
                            .body(DataHelper.getVerificationInfoFor(DataHelper.getAuthInfo(), (String) code)) // передаём в теле объект, который будет преобразован в JSON
                            .when() // "когда"
                            .post("/api/auth/verification") // на какой путь, относительно BaseUri отправляем запрос
                            .then() // "тогда ожидаем"
                            .statusCode(200) // код 200 OK
                            .extract()
                            .path("token");
            System.out.println(token);

            Card[] cards =
                    given() // "дано"
                            .spec(requestSpec) // указываем, какую спецификацию используем
                            .header("Authorization", "Bearer " + token)
                            .when() // "когда"
                            .get("/api/cards") // на какой путь, относительно BaseUri отправляем запрос
                            .then() // "тогда ожидаем"
                            .statusCode(200) // код 200 OK
                            .extract()
                            .as(Card[].class);

            System.out.println(cards[0].getBalance());
            System.out.println(cards[1].getBalance());
            balance1 = Integer.parseInt(cards[0].getBalance());
            balance2 = Integer.parseInt(cards[1].getBalance());

            given() // "дано"
                    .spec(requestSpec) // указываем, какую спецификацию используем
                    .header("Authorization", "Bearer " + token)
                    .body(DataHelper.getTransaction("5559 0000 0000 0002", "5559 0000 0000 0001", sum)) // передаём в теле объект, который будет преобразован в JSON
                    .when() // "когда"
                    .post("/api/transfer") // на какой путь, относительно BaseUri отправляем запрос
                    .then() // "тогда ожидаем"
                    .statusCode(200); // код 200 OK

            Card[] cards2 =
                    given() // "дано"
                            .spec(requestSpec) // указываем, какую спецификацию используем
                            .header("Authorization", "Bearer " + token)
                            .when() // "когда"
                            .get("/api/cards") // на какой путь, относительно BaseUri отправляем запрос
                            .then() // "тогда ожидаем"
                            .statusCode(200) // код 200 OK
                            .extract()
                            .as(Card[].class);

            System.out.println(cards2[0].getBalance());
            System.out.println(cards2[1].getBalance());
            int endBalance1 = Integer.parseInt(cards2[0].getBalance());
            int endBalance2 = Integer.parseInt(cards2[1].getBalance());

            assertEquals(balance1 - sum, endBalance1);
            assertEquals(balance2 + sum, endBalance2);
        }
    }

}
