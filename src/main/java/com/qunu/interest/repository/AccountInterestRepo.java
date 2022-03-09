package com.qunu.interest.repository;

import com.qunu.interest.entity.AccountInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface AccountInterestRepo extends JpaRepository<AccountInterest, Long> {

    List<AccountInterest> findByAccountIdEquals(long accountId);

    @Query("select SUM(a.interest) from AccountInterest a where a.accountId = ?1 and year(a.createdDate) = ?2 and month(a.createdDate) = ?3")
    BigDecimal findByAccountIdEqualsAndCreatedDateYearAndMonth(long accountId, int year, int month);

}
