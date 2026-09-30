package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.Account;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    @EntityGraph(attributePaths = {"roles"})
    Optional<Account> findByUsernameIgnoreCase(String username);

    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT DISTINCT a FROM Account a")
    List<Account> findAllWithRoles();

    boolean existsByUsernameIgnoreCase(String username);
}
