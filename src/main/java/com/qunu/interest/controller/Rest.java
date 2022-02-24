package com.qunu.interest.controller;

import com.qunu.interest.domain.CloseAccount;
import com.qunu.interest.domain.EndOfDayBalances;
import com.qunu.interest.domain.MonthlyInterest;
import com.qunu.interest.domain.OpenAccount;
import com.qunu.interest.services.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity calculateMonthlyInterest(@RequestParam("month") String month) {
        List<MonthlyInterest> monthlyInterest = accountService.getMonthlyInterest(month);
        return ResponseEntity.ok().body(monthlyInterest);
    }

    @PutMapping("/processAccountClosure")
    public ResponseEntity processAccountClosure(@RequestBody CloseAccount request) {
        eventPublisher.publishEvent(request);
        return ResponseEntity.ok().build();
    }

}
