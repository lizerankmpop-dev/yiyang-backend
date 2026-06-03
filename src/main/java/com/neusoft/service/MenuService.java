package com.neusoft.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.entity.Menu;
import com.neusoft.mapper.MenuMapper;
import org.springframework.stereotype.Service;

@Service
public class MenuService extends ServiceImpl<MenuMapper, Menu> {
}
