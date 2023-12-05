package com.naberink.crypto.cryptofiattracker.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class YearOverYearData {
    private final int year;
    private final long totalSatsBalance;
    private final BigDecimal totalAmountEurValueNow;
    private final BigDecimal totalAmountEurAtMomentOfDepositing;
    private final BigDecimal profitComparedToPreviousYear;
    private final BigDecimal depositsThisYear;
}
