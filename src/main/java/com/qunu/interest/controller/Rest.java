package com.qunu.interest.controller;

import com.qunu.interest.domain.CloseAccount;
import com.qunu.interest.domain.EndOfDayBalances;
import com.qunu.interest.domain.MonthlyInterest;
import com.qunu.interest.domain.OpenAccount;
import com.qunu.interest.services.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
public class Rest {

    private final ApplicationEventPublisher eventPublisher;
    private final AccountService accountService;

    @PostMapping("/processAccountOpening")
    public ResponseEntity processAccountOpening(@RequestBody OpenAccount request) {
        eventPublisher.publishEvent(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/processAccountEndOfDayBalances")
    public ResponseEntity processAccountEndOfDayBalances(@RequestBody EndOfDayBalances request) {
        eventPublisher.publishEvent(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Not using async event so we can return the list of monthly interests per account
     */
    @PutMapping("/calculateMonthlyInterest")
    public ResponseEntity<List<MonthlyInterest>> calculateMonthlyInterest(@RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") LocalDate month) {
        List<MonthlyInterest> monthlyInterest = accountService.getMonthlyInterest(month);
        return ResponseEntity.ok().body(monthlyInterest);
    }

    @DeleteMapping("/processAccountClosure")
    public ResponseEntity processAccountClosure(@RequestBody CloseAccount request) {
        eventPublisher.publishEvent(request);
        return ResponseEntity.ok().build();
    }

}
