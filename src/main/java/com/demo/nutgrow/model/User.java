package com.demo.nutgrow.model;

import java.util.HashSet;
import java.util.Set;

import com.demo.nutgrow.model.enums.ProviderEnum;
import com.demo.nutgrow.model.enums.AccountTier;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "Users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
}, catalog = "")
@Data
@EqualsAndHashCode(callSuper = true)

public class User extends AbstractEntity {
    @NotBlank
    @Size(max = 50)
    private String email;

    @Size(max = 120)
    private String password;

    @Column(columnDefinition = "INT DEFAULT 1")
    private Integer status = 1; // 1: active, 0: locked

    private String avatar;

    @Enumerated(EnumType.STRING)
    private ProviderEnum provider;

    private String fullName;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonManagedReference
    private Set<Document> documents = new HashSet<>();;

    @Column
    private Integer role; // 0: admin, 1: user

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'FREE'")
    private AccountTier accountTier = AccountTier.FREE;

    private LocalDateTime subscriptionExpiry;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public Integer getStatus() {
        return status;
    }

    @Override
    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public ProviderEnum getProvider() {
        return provider;
    }

    public void setProvider(ProviderEnum provider) {
        this.provider = provider;
    }

    public AccountTier getAccountTier() {
        return accountTier;
    }

    public void setAccountTier(AccountTier accountTier) {
        this.accountTier = accountTier;
    }

    public LocalDateTime getSubscriptionExpiry() {
        return subscriptionExpiry;
    }

    public void setSubscriptionExpiry(LocalDateTime subscriptionExpiry) {
        this.subscriptionExpiry = subscriptionExpiry;
    }

}
