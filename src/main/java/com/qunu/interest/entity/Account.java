package com.qunu.interest.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int bsb;

    private int identification;

    private BigDecimal balance;

    private BigDecimal interest;

    private LocalDate openingDate;

    private LocalDate balanceUpdateDate;

    private LocalDate interestCalculatedDate;

    private LocalDate closeDate;

    private boolean active;
}
