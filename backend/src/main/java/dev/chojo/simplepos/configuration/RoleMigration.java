package dev.chojo.simplepos.configuration;

import dev.chojo.simplepos.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Gives users that existed before roles were introduced a role: enabled users become ADMIN, so nobody loses access by
 * the upgrade, disabled ones (unapproved signups) become USER. Users created since always get a role, so after the
 * first start this changes nothing.
 */
@Component
public class RoleMigration implements ApplicationRunner {
    private static final Logger log = getLogger(RoleMigration.class);
    private final UserRepository userRepository;

    public RoleMigration(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int migrated = userRepository.assignRoleToUsersWithoutRole();
        if (migrated > 0) log.info("Gave a role to {} users that had none", migrated);
    }
}
