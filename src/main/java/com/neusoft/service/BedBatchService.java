package com.neusoft.service;

import com.neusoft.entity.Bed;
import com.neusoft.mapper.BedMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 床位批量操作 Service
 * 修复：System.out.println → Slf4j log，加 @Transactional 事务保证
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BedBatchService {

    private final BedMapper bedMapper;

    /**
     * 批量修改床位状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateBedStatus(List<Integer> bedIdList, Integer newStatus) {
        String statusStr = newStatus == 0 ? "空闲" : "占用";
        int updated = 0;
        for (Integer bedId : bedIdList) {
            Bed bed = bedMapper.selectById(bedId);
            if (bed != null && !bed.getStatus().equals(newStatus)) {
                bed.setStatus(newStatus);
                bedMapper.updateById(bed);
                updated++;
            }
        }
        log.info("批量修改床位状态完成，共更新 {} 条，状态：{}", updated, statusStr);
    }
}
