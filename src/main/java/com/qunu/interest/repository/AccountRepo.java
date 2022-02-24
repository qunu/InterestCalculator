package com.qunu.interest.repository;

import com.qunu.interest.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepo extends JpaRepository<Account, Long> {
    Optional<Account> findAllByBsbEqualsAndIdentificationEquals(int bsb, int identification);

    Optional<Account> findAllByBsbEqualsAndIdentificationEqualsAndActiveEquals(int bsb, int identification, boolean active);

    List<Account> findAllByBalanceGreaterThanAndActiveTrue(BigDecimal balanceCheck);

}
