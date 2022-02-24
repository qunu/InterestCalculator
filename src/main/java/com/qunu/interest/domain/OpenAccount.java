package com.qunu.interest.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class OpenAccount {

    private int bsb;
    private int identification;
    private LocalDate openingDate;

}
