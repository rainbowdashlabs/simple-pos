package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.controller.exceptions.BadRequestException;
import dev.chojo.simplepos.controller.exceptions.NotFoundException;
import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.dto.AccountDto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    /**
     * Finds an account, including deleted ones, and locks its row until the surrounding transaction ends. Everything
     * that changes an account's balance or closes it takes this lock first, before any ingredient lock, so closing an
     * account cannot race a deposit, a purchase or a cancellation on it.
     *
     * @throws BadRequestException if the id is missing
     * @throws NotFoundException if no account with this id exists
     */
    default Account requireLocked(Integer id) {
        return findByIdForUpdate(requireId(id)).orElseThrow(() -> new NotFoundException("Account", id));
    }

    /**
     * Like {@link #requireLocked(Integer)}, for an account that must not be deleted.
     *
     * @throws NotFoundException if no account with this id exists or it is deleted
     */
    default Account requireNotDeletedLocked(Integer id) {
        Account account = requireLocked(id);
        if (account.isDeleted()) throw new NotFoundException("Account", id);
        return account;
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = ?1")
    Optional<Account> findByIdForUpdate(int id);

    /**
     * Finds an account, including deleted ones.
     *
     * @throws BadRequestException if the id is missing
     * @throws NotFoundException if no account with this id exists
     */
    default Account require(Integer id) {
        return findById(requireId(id)).orElseThrow(() -> new NotFoundException("Account", id));
    }

    /**
     * Finds an account that is not deleted.
     *
     * @throws BadRequestException if the id is missing
     * @throws NotFoundException if no account with this id exists or it is deleted
     */
    default Account requireNotDeleted(Integer id) {
        return findById(requireId(id)).filter(account -> !account.isDeleted())
                .orElseThrow(() -> new NotFoundException("Account", id));
    }

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
            SELECT NEW dev.chojo.simplepos.entity.dto.AccountDto(a.id, a.name, a.created, coalesce(b.balance, 0) - coalesce(p.purchase,0), p.last)
            FROM Account a
            LEFT JOIN (
              SELECT account.id AS id, sum(amount) AS balance
              FROM Balance
              GROUP BY account) b
            ON a.id = b.id
            LEFT JOIN (
              SELECT account.id AS id, sum(amount * price) AS purchase, max(purchased) AS last
              FROM Purchase
              GROUP BY account.id
            ) p
            ON a.id = p.id
            WHERE a.deleted = false
            ORDER BY a.name""")
    List<AccountDto> findAllAccounts();

    private Integer requireId(Integer id) {
        if (id == null) throw new BadRequestException("Account id is missing");
        return id;
    }
}
