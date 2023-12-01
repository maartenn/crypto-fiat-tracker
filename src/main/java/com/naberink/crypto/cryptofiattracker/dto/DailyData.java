package com.naberink.crypto.cryptofiattracker.dto;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Data;

@Data
public class DailyData {
    private Instant day;
    private long totalSatsBalance;
    private BigDecimal totalAmountEurValueNow;
    private BigDecimal totalAmountEurAtMomentOfDepositing;
}
