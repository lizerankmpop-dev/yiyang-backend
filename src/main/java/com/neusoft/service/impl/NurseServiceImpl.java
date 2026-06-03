package com.neusoft.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.entity.Nurse;
import com.neusoft.mapper.NurseMapper;
import com.neusoft.service.NurseService;
import org.springframework.stereotype.Service;

@Service
public class NurseServiceImpl extends ServiceImpl<NurseMapper, Nurse> implements NurseService {
}