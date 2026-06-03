package com.neusoft.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.entity.Food;
import com.neusoft.mapper.FoodMapper;
import org.springframework.stereotype.Service;

@Service
public class FoodService extends ServiceImpl<FoodMapper, Food> {
}
