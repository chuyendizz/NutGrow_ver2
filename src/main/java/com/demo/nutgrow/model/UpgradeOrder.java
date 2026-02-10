package com.demo.nutgrow.model;

import com.demo.nutgrow.model.enums.AccountTier;
import com.demo.nutgrow.model.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "UpgradeOrder")
@Data
@EqualsAndHashCode(callSuper = true)
public class UpgradeOrder extends AbstractEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountTier packageType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    private String paymentProofImage; // URL or base64 of the receipt image

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    private LocalDateTime processedAt;

    @Column(columnDefinition = "TEXT")
    private String adminNote;

}
