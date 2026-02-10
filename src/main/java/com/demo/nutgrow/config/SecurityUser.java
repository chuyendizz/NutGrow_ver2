package com.demo.nutgrow.config;

import com.demo.nutgrow.model.enums.AccountTier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDateTime;
import java.util.Collection;

public class SecurityUser extends User {

    private static final long serialVersionUID = 1L;
    private String email;
    private String provider;
    private AccountTier accountTier;
    private LocalDateTime subscriptionExpiry;

    public SecurityUser(String username, String password, boolean enabled, boolean accountNonExpired,
            boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities,
            String email, String provider, AccountTier accountTier, LocalDateTime subscriptionExpiry) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.email = email;
        this.provider = provider;
        this.accountTier = accountTier;
        this.subscriptionExpiry = subscriptionExpiry;
    }

    public String getEmail() {
        return email;
    }

    public String getProvider() {
        return provider;
    }

    public AccountTier getAccountTier() {
        return accountTier;
    }

    public LocalDateTime getSubscriptionExpiry() {
        return subscriptionExpiry;
    }

    @Override
    public String toString() {
        return "MySecurityUser[email=" + email
                + "] " + super.toString();
    }
}