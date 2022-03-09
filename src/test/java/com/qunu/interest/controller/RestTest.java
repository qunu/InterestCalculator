package com.qunu.interest.controller;

import com.qunu.interest.domain.EndOfDayBalance;
import com.qunu.interest.domain.EndOfDayBalances;
import com.qunu.interest.domain.MonthlyInterest;
import com.qunu.interest.domain.OpenAccount;
import com.qunu.interest.entity.Account;
import com.qunu.interest.entity.AccountInterest;
import com.qunu.interest.repository.AccountInterestRepo;
import com.qunu.interest.repository.AccountRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RestTest {

    @Autowired
    private Rest rest;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private AccountInterestRepo accountInterestRepo;

    @Test
    void given_OpenAccountRequest_when_multiplyOfSame_then_CreateOneAccount() {
        int bsb = 1;
        int identification = 2;
        LocalDate openingDate = LocalDate.now();

        OpenAccount openAccount = OpenAccount.builder().bsb(bsb).identification(identification).openingDate(openingDate).build();

        rest.processAccountOpening(openAccount);
        rest.processAccountOpening(openAccount);
        rest.processAccountOpening(openAccount);
        rest.processAccountOpening(openAccount);

        List<Account> all = accountRepo.findAll();

        assertEquals(1, all.size());
    }

    @Test
    void given_EndOfDayBalancesRequest_when_NoAccountDB_then_DontUpdateOrCreateAccount() {
        int bsb = 1;
        int identification1 = 2;
        int identification2 = 3;

        EndOfDayBalance endOfDayBalance1 = EndOfDayBalance.builder().bsb(bsb).identification(identification1).balance(BigDecimal.ONE).build();
        EndOfDayBalance endOfDayBalance2 = EndOfDayBalance.builder().bsb(bsb).identification(identification2).balance(BigDecimal.TEN).build();
        List<EndOfDayBalance> endOfDayBalance = new ArrayList<>();
        endOfDayBalance.add(endOfDayBalance1);
        endOfDayBalance.add(endOfDayBalance2);

        EndOfDayBalances endOfDayBalances = EndOfDayBalances.builder().balanceDate(LocalDate.now()).endOfDayBalances(endOfDayBalance).build();

        rest.processAccountEndOfDayBalances(endOfDayBalances);

        List<Account> all = accountRepo.findAll();

        assertEquals(0, all.size());
    }

    @Test
    void given_EndOfDayBalancesRequest_when_AccountExists_then_UpdateBalanceAndCalculateInterestAndCreateAuditInterestRecord() {
        int bsb = 1;
        int identification1 = 2;
        int identification2 = 3;
        LocalDate openingDate = LocalDate.now();

        OpenAccount openAccount = OpenAccount.builder().bsb(bsb).identification(identification1).openingDate(openingDate).build();

        rest.processAccountOpening(openAccount);

        EndOfDayBalance endOfDayBalance1 = EndOfDayBalance.builder().bsb(bsb).identification(identification1).balance(new BigDecimal(100)).build();
        EndOfDayBalance endOfDayBalance2 = EndOfDayBalance.builder().bsb(bsb).identification(identification2).balance(BigDecimal.ONE).build();
        List<EndOfDayBalance> endOfDayBalance = new ArrayList<>();
        endOfDayBalance.add(endOfDayBalance1);
        endOfDayBalance.add(endOfDayBalance2);

        EndOfDayBalances endOfDayBalances = EndOfDayBalances.builder().balanceDate(LocalDate.now()).endOfDayBalances(endOfDayBalance).build();

        rest.processAccountEndOfDayBalances(endOfDayBalances);

        List<Account> all = accountRepo.findAll();

        assertEquals(1, all.size());
        Account account = all.get(0);
        assertEquals(openAccount.getBsb(), account.getBsb());
        assertEquals(openAccount.getIdentification(), account.getIdentification());
        assertEquals(openAccount.getOpeningDate(), account.getOpeningDate());
        assertEquals(0, account.getBalance().compareTo(new BigDecimal(100)));
        assertEquals(0, account.getInterest().compareTo(new BigDecimal("0.01")));

        List<AccountInterest> allInterest = accountInterestRepo.findAll();
        assertEquals(1, allInterest.size());
    }

    @Test
    void given_calculateMonthlyInterestRequest_when_AccountExistsAndMultiplyDifferentInterestDaysCalculated_then_ReturnCalculatedInterestForMonth() {
        int bsb = 1;
        int identification1 = 2;
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate dayBeforeYesterday = today.minusDays(2);

        OpenAccount openAccount = OpenAccount.builder().bsb(bsb).identification(identification1).openingDate(dayBeforeYesterday).build();

        rest.processAccountOpening(openAccount);

        EndOfDayBalance endOfDayBalance1 = EndOfDayBalance.builder()
                .bsb(bsb)
                .identification(identification1)
                .balance(new BigDecimal(200))
                .build();

        List<EndOfDayBalance> endOfDayBalance = new ArrayList<>();
        endOfDayBalance.add(endOfDayBalance1);

        EndOfDayBalances endOfDayBalances = EndOfDayBalances.builder().balanceDate(dayBeforeYesterday).endOfDayBalances(endOfDayBalance).build();
        EndOfDayBalances endOfDayBalancesYesterday = EndOfDayBalances.builder().balanceDate(yesterday).endOfDayBalances(endOfDayBalance).build();
        EndOfDayBalances endOfDayBalancesToday = EndOfDayBalances.builder().balanceDate(today).endOfDayBalances(endOfDayBalance).build();

        rest.processAccountEndOfDayBalances(endOfDayBalances);
        rest.processAccountEndOfDayBalances(endOfDayBalancesYesterday);
        rest.processAccountEndOfDayBalances(endOfDayBalancesToday);

        List<Account> all = accountRepo.findAll();

        assertEquals(1, all.size());
        Account account = all.get(0);
        assertEquals(openAccount.getBsb(), account.getBsb());
        assertEquals(openAccount.getIdentification(), account.getIdentification());
        assertEquals(openAccount.getOpeningDate(), account.getOpeningDate());
        assertEquals(0, account.getBalance().compareTo(new BigDecimal(200)));
        assertEquals(0, account.getInterest().compareTo(new BigDecimal("0.09")));

        List<AccountInterest> allInterest = accountInterestRepo.findAll();
        assertEquals(3, allInterest.size());
        assertEquals(1, allInterest.stream().filter(accountInterest ->
                accountInterest.getCreatedDate().equals(dayBeforeYesterday)).count());
        assertEquals(1, allInterest.stream().filter(accountInterest ->
                accountInterest.getCreatedDate().equals(yesterday)).count());
        assertEquals(1, allInterest.stream().filter(accountInterest ->
                accountInterest.getCreatedDate().equals(today)).count());

        ResponseEntity responseEntity = rest.calculateMonthlyInterest(LocalDate.now());
        List<MonthlyInterest> body = (List<MonthlyInterest>) responseEntity.getBody();
        assertEquals(1, body.size());


        BigDecimal dayInterest = new BigDecimal("0.03");
        BigDecimal monthTotal = new BigDecimal("0.03");

        if (yesterday.getMonth() == today.getMonth()) {
            monthTotal = monthTotal.add(dayInterest);
        }
        if (dayBeforeYesterday.getMonth() == today.getMonth()) {
            monthTotal = monthTotal.add(dayInterest);
        }

        assertEquals(0, body.get(0).getInterest().compareTo(monthTotal));
    }

}