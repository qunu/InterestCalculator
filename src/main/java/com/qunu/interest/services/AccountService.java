package com.qunu.interest.services;

import com.qunu.interest.config.ApplicationConfig;
import com.qunu.interest.domain.*;
import com.qunu.interest.entity.Account;
import com.qunu.interest.entity.AccountInterest;
import com.qunu.interest.repository.AccountInterestRepo;
import com.qunu.interest.repository.AccountRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final ApplicationConfig applicationConfig;
    private final AccountRepo accountRepo;
    private final AccountInterestRepo accountInterestRepo;

    /**
     * Find if account already exists
     * If account exists that is not active then set to active and set Opening date to received Opening date
     * If account exists that is active then return (Job done)
     * If no account exists then create new account
     */
    @Async
    @EventListener
    public void openAccount(OpenAccount request) {
        //look for account
        Optional<Account> byId = accountRepo.findAllByBsbEqualsAndIdentificationEquals(request.getBsb(), request.getIdentification());

        if (byId.isPresent()) {
            Account account = byId.get();
            //
            if (!account.isActive()) {
                account.setActive(true);
                account.setOpeningDate(request.getOpeningDate());
                accountRepo.save(account);
            }
            return;
        }

        //create if not found
        Account account = Account.builder()
                .bsb(request.getBsb())
                .identification(request.getIdentification())
                .openingDate(request.getOpeningDate())
                .active(true)
                .build();

        accountRepo.save(account);
    }

    /**
     * Calculate daily interest rate
     * Update Received Balances
     * Get active accounts where Balance greater than 0
     * Calculate Interest
     */
    @Async
    @EventListener
    public void getAccruedInterest(EndOfDayBalances request) {
        BigDecimal dailyInterest = BigDecimal.valueOf(applicationConfig.getAnnualInterest() / 100 / 365);
        LocalDate requestBalanceDate = request.getBalanceDate();

        updateReceivedBalances(request, requestBalanceDate);

        //Get all balances above 0 and active true (NO CLOSED ACCOUNTS)
        List<Account> allByBalanceGreaterThan = accountRepo.findAllByBalanceGreaterThanAndActiveTrue(BigDecimal.ZERO);

        if (CollectionUtils.isEmpty(allByBalanceGreaterThan)) {
            return;
        }

        calculateInterest(dailyInterest, requestBalanceDate, allByBalanceGreaterThan);
    }

    /**
     * Get all AccountInterest for the month received per account and sum up the interests
     */
    public List<MonthlyInterest> getMonthlyInterest(String month) {
        List<MonthlyInterest> result = new ArrayList<>();
        List<Account> all = accountRepo.findAll();
        all.forEach(account -> {
            var totalWrapper = new Object() {
                BigDecimal monthlyInterestResult = BigDecimal.ZERO;
            };
            List<AccountInterest> byAccountIdEquals = accountInterestRepo.findByAccountIdEquals(account.getId());
            List<AccountInterest> collect = byAccountIdEquals.stream()
                    .filter(accountInterest -> StringUtils.equals(month, accountInterest.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))))
                    .collect(Collectors.toList());

            collect.forEach(accountInterest ->
                    totalWrapper.monthlyInterestResult = totalWrapper.monthlyInterestResult.add(accountInterest.getInterest())
            );
            MonthlyInterest monthlyInterest = MonthlyInterest.builder().bsb(account.getBsb()).identification(account.getIdentification()).interest(totalWrapper.monthlyInterestResult).build();
            result.add(monthlyInterest);
        });
        return result;
    }

    /**
     * Find Account and set Active to false and Close Date to now
     */
    @Async
    @EventListener
    public void closeAccount(CloseAccount request) {
        Optional<Account> accountOptional = accountRepo.findAllByBsbEqualsAndIdentificationEquals(request.getBsb(), request.getIdentification());

        if (accountOptional.isPresent() && !accountOptional.get().isActive()) {
            Account account = accountOptional.get();
            account.setActive(false);
            account.setCloseDate(LocalDate.now());
            accountRepo.save(account);
        } else {
            log.info("No account exists or account already closed in Data-store for {} {}", request.getBsb(), request.getIdentification());
        }
    }


    /**
     * Traverse through list of accounts and save Balance and update BalanceUpdate in data-store if accounts exists
     * and BalanceUpdate in data-store is older than received BalanceDate
     */
    private void updateReceivedBalances(EndOfDayBalances request, LocalDate requestBalanceDate) {
        List<EndOfDayBalance> endOfDayBalances = request.getEndOfDayBalances();
        endOfDayBalances.forEach(accountBalances -> {
            Optional<Account> accountOptional = accountRepo.findAllByBsbEqualsAndIdentificationEquals(accountBalances.getBsb(), accountBalances.getIdentification());
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();
                boolean updateBalance = account.getBalanceUpdateDate() == null || account.getBalanceUpdateDate().isBefore(requestBalanceDate);
                if (updateBalance) {
                    account.setBalance(accountBalances.getBalance());
                    account.setBalanceUpdateDate(requestBalanceDate);
                    accountRepo.save(account);
                }
            } else {
                log.info("No account exists in Data-store for {} {}", accountBalances.getBsb(), accountBalances.getIdentification());
            }
        });
    }

    /**
     * Traverse through Account's and continue if interest has not been calculated for the request Date
     * Create AccountInterest record
     * Update Account InterestCalculated date and Interest
     */
    private void calculateInterest(BigDecimal dailyInterest, LocalDate requestBalanceDate, List<Account> allByBalanceGreaterThan) {
        allByBalanceGreaterThan.stream().forEach(account -> {
            boolean calculateInterest = account.getInterestCalculatedDate() == null || account.getInterestCalculatedDate().isBefore(requestBalanceDate);

            //only calculate interest if InterestCalculated in the past
            //If InterestCalculated is today then we have already done the calculation
            if (calculateInterest) {
                BigDecimal interestEarned = account.getBalance().multiply(dailyInterest);

                //Save into interest audit table
                AccountInterest accountInterest = AccountInterest.builder()
                        .accountId(account.getId())
                        .createdDate(requestBalanceDate)
                        .interest(interestEarned)
                        .build();
                accountInterestRepo.save(accountInterest);

                //update account
                account.setInterestCalculatedDate(requestBalanceDate);
                account.setInterest((account.getInterest() == null ? BigDecimal.ZERO : account.getInterest()).add(interestEarned));
                accountRepo.save(account);
            }
        });
    }
}
