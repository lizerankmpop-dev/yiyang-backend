package com.neusoft.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.entity.Room;
import com.neusoft.mapper.RoomMapper;
import org.springframework.stereotype.Service;

@Service
public class RoomService extends ServiceImpl<RoomMapper, Room> {
}
