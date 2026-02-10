package com.demo.nutgrow.config;

import com.demo.nutgrow.exception.UserBlockedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String errorMessage = "Tên đăng nhập hoặc mật khẩu không đúng";

        if (exception instanceof InternalAuthenticationServiceException
                && exception.getCause() instanceof UserBlockedException) {

            errorMessage = exception.getCause().getMessage();

        }
        else if (exception instanceof UserBlockedException) {

            errorMessage = exception.getMessage();

        }
        else if (exception instanceof BadCredentialsException) {

            errorMessage = "Tên đăng nhập hoặc mật khẩu không đúng";

        }

        response.sendRedirect(
                "/login?error=true&message="
                        + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8)
        );
    }
}
