package com.neusoft.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.entity.NurseCustomer;
import com.neusoft.mapper.NurseCustomerMapper;
import org.springframework.stereotype.Service;

@Service
public class NurseCustomerService extends ServiceImpl<NurseCustomerMapper, NurseCustomer> {
}
