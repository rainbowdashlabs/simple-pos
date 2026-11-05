package dev.chojo.simplepos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Synchronize;

import java.math.BigDecimal;

/**
 * An account's balance: its deposits minus its purchases. Read from the {@code account_balances} view, the one place
 * this rule is written down. Pending changes to the tables it reads are written before it is queried.
 */
@Entity
@Immutable
@Synchronize({"account", "balance", "purchase"})
@Table(name = "account_balances")
public class AccountBalance {
    @Id
    @Column(name = "account_id")
    private Integer accountId;

    private BigDecimal balance;

    public Integer getAccountId() {
        return accountId;
    }

    public BigDecimal getBalance() {
        return Money.of(balance);
    }
}
