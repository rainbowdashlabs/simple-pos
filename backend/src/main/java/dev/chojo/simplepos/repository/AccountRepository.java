package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.dto.AccountDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    @Query("""
            SELECT NEW dev.chojo.simplepos.entity.dto.AccountDto(a.id, a.name, a.created, coalesce(b.balance, 0) - coalesce(p.purchase,0))
            FROM Account a
            LEFT JOIN (
              SELECT account.id AS id, sum(amount) AS balance
              FROM Balance
              WHERE account.id = ?1
              GROUP BY account) b
            ON a.id = b.id
            LEFT JOIN (
              SELECT account.id AS id, sum(amount * price) AS purchase
              FROM Purchase
              WHERE account.id = ?1
              GROUP BY account.id
            ) p
            ON a.id = p.id
            WHERE a.id = ?1""")
    Optional<AccountDto> findAccountById(int id);

    @Query("""
            SELECT NEW dev.chojo.simplepos.entity.dto.AccountDto(a.id, a.name, a.created, coalesce(b.balance, 0) - coalesce(p.purchase,0))
            FROM Account a
            LEFT JOIN (
              SELECT account.id AS id, sum(amount) AS balance
              FROM Balance
              GROUP BY account) b
            ON a.id = b.id
            LEFT JOIN (
              SELECT account.id AS id, sum(amount * price) AS purchase
              FROM Purchase
              GROUP BY account.id
            ) p
            ON a.id = p.id ORDER BY a.name""")
    List<AccountDto> findAllAccounts();
}
