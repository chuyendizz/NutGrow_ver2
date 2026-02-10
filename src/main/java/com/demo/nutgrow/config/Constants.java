package com.demo.nutgrow.config;

public class Constants {
    public String otpCode() {
        int code = (int) Math.floor(((Math.random() * 899999) + 100000));
        return String.valueOf(code);
    }
}
