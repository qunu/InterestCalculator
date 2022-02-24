package com.qunu.interest.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class EndOfDayBalance {

    private int bsb;
    private int identification;
    private BigDecimal balance;
}
