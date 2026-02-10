package com.demo.nutgrow.service;

import com.demo.nutgrow.model.User;
import com.demo.nutgrow.model.enums.ProviderEnum;
import com.demo.nutgrow.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OtpService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean verifyOtpAndCreateUser(HttpSession session, String otp) {
        String otpRegister = (String) session.getAttribute("otp-register");

        if (otp.equals(otpRegister)) {
            User user = new User();
            user.setEmail((String) session.getAttribute("email"));
            user.setFullName((String) session.getAttribute("fullName"));
            user.setPassword(passwordEncoder.encode((String) session.getAttribute("password")));
            user.setStatus(1);
            user.setRole(1);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setCreatedBy("ADMIN");
            user.setUpdatedBy("ADMIN");
            user.setProvider(ProviderEnum.LOCAL);

            userRepository.save(user);
            return true;
        }

        return false;
    }
}
