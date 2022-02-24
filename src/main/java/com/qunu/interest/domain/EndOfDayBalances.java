package com.qunu.interest.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class EndOfDayBalances {

    private LocalDate balanceDate;
    private List<EndOfDayBalance> endOfDayBalances;

}
