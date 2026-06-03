package com.neusoft.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.neusoft.entity.Bed;
import com.neusoft.entity.Customer;
import com.neusoft.mapper.BedMapper;
import com.neusoft.mapper.CheckInRecordMapper;
import com.neusoft.mapper.CustomerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

@Service
public class CheckInService {

    @Resource
    private BedMapper bedMapper;
    @Resource
    private CustomerMapper customerMapper;
    @Resource
    private CheckInRecordMapper checkInRecordMapper;

    /**
     * 办理入住：
     * 1. 保存客户信息（生成ID）
     * 2. 分配空闲床位（取第一个）
     * 3. 更新客户：isCheckIn=1, bedId=床位ID, checkInDate=今天
     * 4. 更新床位：status=1（已占用）
     * 5. 写入入住记录
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean checkInCustomer(List<Bed> availableBeds, Customer customer) {
        // 1. 先保存客户，生成ID
        customer.setIsCheckIn(0); // 暂未入住（等分配床位后再改为1）
        customer.setIsOuting(0);
        customer.setCheckInDate(LocalDate.now());
        customerMapper.insert(customer); // 保存后 customer.getId() 有值了

        // 2. 取第一个空闲床位
        Bed bed = availableBeds.get(0);
        Integer bedId = bed.getId();
        String bedNo = bed.getBedNo();

        // 3. 更新客户：绑定床位、标记已入住
        LambdaUpdateWrapper<Customer> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Customer::getId, customer.getId())
                .set(Customer::getIsCheckIn, 1)
                .set(Customer::getBedId, bedId)
                .set(Customer::getIsOuting, 0);
        customerMapper.update(null, updateWrapper);

        // 4. 更新床位状态为已占用
        bed.setStatus(1);
        bedMapper.updateById(bed);

        // 5. 写入入住记录
        checkInRecordMapper.addCheckInRecord(bedId, customer.getId());

        System.out.printf("入住成功：客户=%s, 床位=%s%n", customer.getName(), bedNo);
        return true;
    }
}
