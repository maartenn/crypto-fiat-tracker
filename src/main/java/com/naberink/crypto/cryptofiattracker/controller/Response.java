package com.naberink.crypto.cryptofiattracker.controller;

import java.math.BigDecimal;
import java.util.List;

import com.naberink.crypto.cryptofiattracker.dto.DailyData;
import com.naberink.crypto.cryptofiattracker.dto.MappedTransaction;
import com.naberink.crypto.cryptofiattracker.dto.YearOverYearData;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Response {
    List<DailyData> dailyData; // used by chart
    List<MappedTransaction> mappedTransactions; // used by table
    BigDecimal netEurDeposited;
    BigDecimal totalEurValueNow;
    Long sats;
    BigDecimal percentProfit;
}
