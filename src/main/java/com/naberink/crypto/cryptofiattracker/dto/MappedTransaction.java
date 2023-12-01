package com.naberink.crypto.cryptofiattracker.dto;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MappedTransaction {
    private String txId;
    private long txAmountSats; // negative means sell
    private BigDecimal txAmountEur;
    private BigDecimal totalAmountEurAtMomentOfDepositing;
    private Instant timestamp;
    private long totalSatsBalance;
    private BigDecimal totalAmountEurValueNow;
    private BigDecimal txAmountEurNow;
}
