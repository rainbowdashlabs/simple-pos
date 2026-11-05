package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Account;
import dev.chojo.simplepos.entity.dto.AccountDto;
import dev.chojo.simplepos.exceptions.BadRequestException;
import dev.chojo.simplepos.exceptions.NotFoundException;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends LookupRepository<Account> {
    @Override
    default String entityName() {
        return "Account";
    }

    /**
     * Finds an account, including deleted ones, and locks its row until the surrounding transaction ends. Everything
     * that changes an account's balance or closes it takes this lock first, before any ingredient lock, so closing an
     * account cannot race a deposit, a purchase or a cancellation on it.
     *
     * @throws BadRequestException if the id is missing
     * @throws NotFoundException if no account with this id exists
     */
    default Account requireLocked(Integer id) {
        return findByIdForUpdate(requireId(id)).orElseThrow(() -> new NotFoundException(entityName(), id));
    }

    /**
     * Like {@link #requireLocked(Integer)}, for an account that must not be deleted.
     *
     * @throws NotFoundException if no account with this id exists or it is deleted
     */
    default Account requireNotDeletedLocked(Integer id) {
        Account account = requireLocked(id);
        if (account.isDeleted()) throw new NotFoundException(entityName(), id);
        return account;
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = ?1")
    Optional<Account> findByIdForUpdate(int id);

    /**
     * Finds an account that is not deleted.
     *
     * @throws BadRequestException if the id is missing
     * @throws NotFoundException if no account with this id exists or it is deleted
     */
    default Account requireNotDeleted(Integer id) {
        return findById(requireId(id)).filter(account -> !account.isDeleted())
                .orElseThrow(() -> new NotFoundException(entityName(), id));
    }

    @Query("""
            SELECT NEW dev.chojo.simplepos.entity.dto.AccountDto(a.id, a.name, a.created, b.balance)
            FROM Account a
            JOIN AccountBalance b ON b.accountId = a.id
            WHERE a.id = ?1""")
    Optional<AccountDto> findAccountById(int id);

    /**
     * The open accounts whose lower-case name matches the {@code contains} LIKE pattern ({@code !} escapes): the one
     * named exactly {@code exact} first, then those matching the {@code prefix} pattern, then the rest, each by name.
     * So a short name contained in many others still comes first when it is typed out.
     */
    @Query(value = """
            SELECT NEW dev.chojo.simplepos.entity.dto.AccountDto(a.id, a.name, a.created, b.balance, p.last)
            FROM Account a
            JOIN AccountBalance b ON b.accountId = a.id
            LEFT JOIN (
              SELECT account.id AS id, max(purchased) AS last
              FROM Purchase
              WHERE account IS NOT NULL
              GROUP BY account.id
            ) p
            ON a.id = p.id
            WHERE a.deleted = false AND lower(a.name) LIKE :contains ESCAPE '!'
            ORDER BY CASE WHEN lower(a.name) = :exact THEN 0 WHEN lower(a.name) LIKE :prefix ESCAPE '!' THEN 1 ELSE 2 END,
                     a.name, a.id""",
            countQuery = "SELECT count(a) FROM Account a WHERE a.deleted = false AND lower(a.name) LIKE :contains ESCAPE '!'")
    Page<AccountDto> findAccounts(@Param("exact") String exact, @Param("prefix") String prefixPattern,
                                  @Param("contains") String containsPattern, Pageable pageable);

    /**
     * The open accounts that bought something, latest buyer first.
     */
    @Query("""
            SELECT NEW dev.chojo.simplepos.entity.dto.AccountDto(a.id, a.name, a.created, b.balance, p.last)
            FROM Account a
            JOIN AccountBalance b ON b.accountId = a.id
            JOIN (
              SELECT account.id AS id, max(purchased) AS last
              FROM Purchase
              WHERE account IS NOT NULL
              GROUP BY account.id
            ) p
            ON a.id = p.id
            WHERE a.deleted = false
            ORDER BY p.last DESC, a.id""")
    List<AccountDto> findRecentBuyers(Pageable limit);
}
