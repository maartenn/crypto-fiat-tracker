package com.naberink.crypto.cryptofiattracker.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class YearOverYearData {
    private final int year;
    private final long totalSatsBalance;
    private final BigDecimal totalAmountEurValueNow;
    private final BigDecimal totalAmountEurAtMomentOfDepositing;
    private final BigDecimal profitComparedToPreviousYear;
}
