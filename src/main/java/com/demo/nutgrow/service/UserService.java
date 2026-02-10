package com.demo.nutgrow.service;

import com.demo.nutgrow.config.SecurityUser;
import com.demo.nutgrow.exception.UserBlockedException;
import com.demo.nutgrow.model.User;
import com.demo.nutgrow.model.enums.ProviderEnum;
import com.demo.nutgrow.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    public boolean validateCredentials(String username, String password) {
        return userRepository.existsByEmailAndPassword(username, passwordEncoder.encode(password));
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Optional<User> userByUsername = userRepository.findByEmail(username);
            if (userByUsername.isEmpty()) {
                throw new UsernameNotFoundException("Invalid credentials!");
            }
            User user = userByUsername.get();

            if (user.getStatus() == null || user.getStatus() != 1) {
                throw new UserBlockedException("Tài khoản của bạn đã bị khóa!");
            }

            Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
            String role = "ROLE_";
            Integer roleUser = user.getRole();
            if (roleUser == 0) {
                role += "ADMIN";
            } else if (roleUser == 1) {
                role += "USER";
            }

            grantedAuthorities.add(new SimpleGrantedAuthority(role));

            return new SecurityUser(user.getEmail(), user.getPassword(), true, true, true, true, grantedAuthorities,
                    user.getEmail(), user.getProvider().name(), user.getAccountTier(), user.getSubscriptionExpiry());
        } catch (UserBlockedException | UsernameNotFoundException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new UsernameNotFoundException("Invalid credentials!");
        }
    }

    public String validateUserInput(String email, String fullName, String password) {

        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return "Email không hợp lệ!";
        }

        if (emailExists(email)) {
            return "Email đã tồn tại. Hãy nhập Email mới!";
        }

        if (!fullName.matches("^[\\p{L} .'-]+$")) {
            return "Họ và tên không hợp lệ! Vui lòng chỉ nhập chữ.";
        }

        String passwordValidation = validatePassword(password);
        if (passwordValidation != null) {
            return passwordValidation;
        }

        return null;
    }

    public void saveUser(String email, String fullName, String password) {
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setStatus(1);
        user.setRole(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setCreatedBy("ADMIN");
        user.setUpdatedBy("ADMIN");
        user.setProvider(ProviderEnum.LOCAL);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    private boolean emailExists(String email) {
        return findByEmail(email).isPresent();
    }

    private String validatePassword(String password) {
        if (password.length() < 7) {
            return "Mật khẩu phải có ít nhất 7 ký tự!";
        } else if (!password.matches(".*[a-z].*")) {
            return "Mật khẩu phải có ít nhất 1 chữ thường!";
        } else if (!password.matches(".*[A-Z].*")) {
            return "Mật khẩu phải có ít nhất 1 chữ hoa!";
        } else if (!password.matches(".*\\d.*")) {
            return "Mật khẩu phải có ít nhất 1 số!";
        } else if (!password.matches(".*[@$!%*?&].*")) {
            return "Mật khẩu phải có ít nhất 1 ký tự đặc biệt (@$!%*?&)!";
        } else if (!password.matches("^[A-Za-z\\d@$!%*?&]{7,}$")) {
            return "Mật khẩu chỉ được chứa chữ cái, số và ký tự đặc biệt @$!%*?&!";
        }
        return null;
    }

    @Transactional
    public Boolean processOAuthPostLogin(String email, String fullName) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getStatus() == 0)
                return false;
        } else {
            User User = new User();
            User.setEmail(email);
            User.setStatus(1);
            User.setRole(1);
            User.setCreatedAt(LocalDateTime.now());
            User.setUpdatedAt(LocalDateTime.now());
            User.setCreatedBy("ADMIN");
            User.setUpdatedBy("ADMIN");
            User.setProvider(ProviderEnum.GOOGLE);
            User.setPassword(passwordEncoder.encode("123"));
            userRepository.save(User);
        }
        return true;
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public long countUsers() {
        return userRepository.count();
    }

    public boolean saveNewUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        user.setProvider(ProviderEnum.LOCAL);

        userRepository.save(user);

        return true;
    }

    public boolean toggleStatus(Integer id, Integer newStatus) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus(newStatus);
            user = userRepository.save(user);
            user.setStatus(newStatus);
            userRepository.save(user);
            return true;
        }
        return false;
    }

}
