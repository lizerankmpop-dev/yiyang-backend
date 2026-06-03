package com.neusoft.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.entity.Nurse;
import com.neusoft.entity.NursingTask;
import com.neusoft.entity.StatisticsData;
import com.neusoft.entity.TrendData;
import com.neusoft.mapper.CustomerMapper;
import com.neusoft.mapper.NurseMapper;
import com.neusoft.mapper.NursingTaskMapper;
import com.neusoft.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final CustomerMapper customerMapper;
    private final NurseMapper nurseMapper;
    private final NursingTaskMapper nursingTaskMapper;

    /**
     * 护工工作量（简化实现，兼容你的现有表结构）
     */
    @Override
    public List<StatisticsData> getNurseWorkload() {
        List<StatisticsData> list = new ArrayList<>();
        StatisticsData data = new StatisticsData();
        data.setName("默认护工");
        data.setValue(0);
        list.add(data);
        return list;
    }

    /**
     * 近7天入住趋势（调用你Mapper里的countInByDate）
     */
    @Override
    public List<TrendData> getRecentCheckInTrend() {
        List<TrendData> trendList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).format(formatter);
            TrendData data = new TrendData();
            data.setDate(date);
            data.setCount(customerMapper.countInByDate(date));
            trendList.add(data);
        }
        return trendList;
    }

    /**
     * 近7天退住趋势（调用你Mapper里的countOutByDate）
     */
    @Override
    public List<TrendData> getRecentCheckOutTrend() {
        List<TrendData> trendList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).format(formatter);
            TrendData data = new TrendData();
            data.setDate(date);
            data.setCount(customerMapper.countOutByDate(date));
            trendList.add(data);
        }
        return trendList;
    }

    /**
     * 近30天客户数量趋势：每天统计截止该日的在住客户总数
     */
    @Override
    public List<TrendData> getCustomerCountTrend() {
        List<TrendData> trendList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 29; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).format(formatter);
            TrendData data = new TrendData();
            data.setDate(date);
            data.setCount(customerMapper.countInHouseByDate(date));
            trendList.add(data);
        }
        return trendList;
    }

    /**
     * 近30天人力占用率趋势：每天统计有任务的护工数 / 在职护工总数（百分比）
     */
    @Override
    public List<TrendData> getNurseUtilizationTrend() {
        List<TrendData> trendList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 在职护工总数
        LambdaQueryWrapper<Nurse> nurseQuery = new LambdaQueryWrapper<>();
        nurseQuery.eq(Nurse::getIsDeleted, 0).eq(Nurse::getStatus, 1);
        long totalNurses = nurseMapper.selectCount(nurseQuery);

        for (int i = 29; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).format(formatter);
            TrendData data = new TrendData();
            data.setDate(date);

            if (totalNurses == 0) {
                data.setCount(0);
            } else {
                // 当天有任务的护工数（去重）
                LambdaQueryWrapper<NursingTask> taskQuery = new LambdaQueryWrapper<>();
                taskQuery.eq(NursingTask::getTaskDate, date);
                List<NursingTask> tasks = nursingTaskMapper.selectList(taskQuery);
                long nursesWithTasks = tasks.stream()
                        .map(NursingTask::getNurseId)
                        .filter(id -> id != null)
                        .distinct()
                        .count();
                // 转换为百分比（0-100），四舍五入
                int rate = (int) Math.round(nursesWithTasks * 100.0 / totalNurses);
                data.setCount(rate);
            }

            trendList.add(data);
        }
        return trendList;
    }
}
