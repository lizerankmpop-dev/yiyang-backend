package com.neusoft.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.entity.NurseLeave;
import com.neusoft.mapper.NurseLeaveMapper;
import com.neusoft.service.NurseLeaveService;
import org.springframework.stereotype.Service;

@Service
public class NurseLeaveServiceImpl extends ServiceImpl<NurseLeaveMapper, NurseLeave> implements NurseLeaveService {
}
