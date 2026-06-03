package com.neusoft.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.entity.NursingLevel;
import com.neusoft.entity.NursingLevelItem;
import com.neusoft.mapper.NursingLevelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 护理级别 Service
 * 修复：
 * 1. 移除 nextRelationId()（遍历全表取 max+1），改为数据库自增
 * 2. 使用 @Transactional 保证批量操作原子性
 * 3. 使用 Stream 替代 for 循环，代码更简洁
 */
@Service
@RequiredArgsConstructor
public class NursingLevelService extends ServiceImpl<NursingLevelMapper, NursingLevel> {

    private final NursingLevelItemService nursingLevelItemService;

    /**
     * 获取某护理级别关联的护理项目 ID 列表
     */
    public List<Integer> getItemIds(Integer levelId) {
        return nursingLevelItemService.lambdaQuery()
                .eq(NursingLevelItem::getLevelId, levelId)
                .list()
                .stream()
                .map(NursingLevelItem::getItemId)
                .collect(Collectors.toList());
    }

    /**
     * 保存护理级别与护理项目的关联（先删后插）
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveLevelItems(Integer levelId, List<Integer> itemIds) {
        // 先删除旧关联
        nursingLevelItemService.lambdaUpdate()
                .eq(NursingLevelItem::getLevelId, levelId)
                .remove();

        if (itemIds == null || itemIds.isEmpty()) {
            return;
        }

        // 构建新关联（id 由数据库自增，无需手动计算）
        List<NursingLevelItem> relations = itemIds.stream().map(itemId -> {
            NursingLevelItem rel = new NursingLevelItem();
            rel.setLevelId(levelId);
            rel.setItemId(itemId);
            return rel;
        }).collect(Collectors.toList());

        nursingLevelItemService.saveBatch(relations);
    }
}
