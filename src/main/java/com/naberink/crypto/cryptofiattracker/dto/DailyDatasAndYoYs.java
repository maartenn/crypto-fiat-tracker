package com.naberink.crypto.cryptofiattracker.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailyDatasAndYoYs {
    private List<DailyData> dailyDataList;
    private List<YearOverYearData> yearOverYearDataList;
}
