package com.neusoft.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.entity.Bed;
import com.neusoft.mapper.BedMapper;
import org.springframework.stereotype.Service;

@Service
public class BedService extends ServiceImpl<BedMapper, Bed> {
}