package com.neusoft.service;

import com.neusoft.entity.StatisticsData;
import com.neusoft.entity.TrendData;
import java.util.List;

public interface StatisticsService {
    // 护工工作量排名
    List<StatisticsData> getNurseWorkload();
    // 近7天入住趋势
    List<TrendData> getRecentCheckInTrend();
    // 近7天退住趋势
    List<TrendData> getRecentCheckOutTrend();
    // 近30天客户数量趋势（在住客户累计）
    List<TrendData> getCustomerCountTrend();
    // 近30天人力占用率趋势（有任务的护工数/总护工数）
    List<TrendData> getNurseUtilizationTrend();
}