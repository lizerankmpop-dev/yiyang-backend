package com.neusoft.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.entity.Meal;
import com.neusoft.mapper.MealMapper;
import org.springframework.stereotype.Service;

@Service
public class MealService extends ServiceImpl<MealMapper, Meal> {
}
