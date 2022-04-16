package ru.netology.domain;

import lombok.Value;


public class DataHelper {
    private DataHelper() {

    }

    @Value
    public static class AuthInfo {
        private String login;
        private String password;
    }


    public static AuthInfo getAuthInfo() {
        return new AuthInfo("vasya", "qwerty123");
    }

    @Value
    public static class VerificationInfo {
        private String login;
        private String code;
    }

    public static VerificationInfo getVerificationInfoFor(AuthInfo authInfo, String code) {
        return new VerificationInfo(authInfo.getLogin(), code);
    }

    @Value
    public static class Transaction {
        private String from;
        private String to;
        private int amount;
    }

    public static Transaction getTransaction(String from, String to, int amount) {
        return new Transaction(from, to, amount);
    }


}
