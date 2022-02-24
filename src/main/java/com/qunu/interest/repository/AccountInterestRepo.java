package com.qunu.interest.repository;

import com.qunu.interest.entity.AccountInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountInterestRepo extends JpaRepository<AccountInterest, Long> {

    List<AccountInterest> findByAccountIdEquals(long accountId);

}
