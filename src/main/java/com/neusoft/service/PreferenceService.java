package com.neusoft.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.entity.Preference;
import com.neusoft.mapper.PreferenceMapper;
import org.springframework.stereotype.Service;

@Service
public class PreferenceService extends ServiceImpl<PreferenceMapper, Preference> {
}
