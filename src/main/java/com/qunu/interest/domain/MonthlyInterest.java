package com.qunu.interest.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class MonthlyInterest {

    private int bsb;
    private int identification;
    private BigDecimal interest;
}
