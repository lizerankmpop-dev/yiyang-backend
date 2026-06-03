package com.neusoft.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.common.R;
import com.neusoft.dto.CustomerExcelDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.entity.Customer;
import com.neusoft.entity.Bed;
import com.neusoft.entity.NursingLevel;
import com.neusoft.entity.Backdown;
import com.neusoft.listener.CustomerExcelListener;
import com.neusoft.service.CustomerService;
import com.neusoft.mapper.CheckInRecordMapper;
import com.neusoft.mapper.BedMapper;
import com.neusoft.mapper.CustomerMapper;
import com.neusoft.mapper.NursingLevelMapper;
import com.neusoft.service.BackdownService;
import com.neusoft.utils.OperationLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户/老人信息管理控制器
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final BedMapper bedMapper;
    private final NursingLevelMapper nursingLevelMapper;
    private final BackdownService backdownService;
    private final OperationLogger operationLogger;
    private final HttpServletRequest httpRequest;

    /**
     * 分页列表（修复：异常捕获 + 防崩溃 + 解决页面卡死）
     */
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) Integer levelId,
            @RequestParam(required = false) String hasTags) {
        log.info("【客户管理】分页查询，page={}, size={}, name={}, id={}, levelId={}, hasTags={}", page, size, name, id, levelId, hasTags);

        try {
            // 1. 查询全量数据（先过滤 is_deleted=0 + 关联床位+护理级别）
            List<Customer> records = customerService.listWithBedAndNurseLevel(name);
            if (records == null) {
                records = Collections.emptyList();
            }

            // 2. 附加过滤条件（从Dashboard跳转时携带）
            if (id != null) {
                final Integer cid = id;
                records = records.stream()
                        .filter(c -> cid.equals(c.getId()))
                        .collect(Collectors.toList());
            }
            if (levelId != null) {
                final Integer lvId = levelId;
                records = records.stream()
                        .filter(c -> lvId.equals(c.getLevelId()))
                        .collect(Collectors.toList());
            }
            if ("1".equals(hasTags)) {
                records = records.stream()
                        .filter(c -> c.getTags() != null && !c.getTags().trim().isEmpty())
                        .collect(Collectors.toList());
            }

            // 3. 分页（避免subList越界）
            int total = records.size();
            int start = (page - 1) * size;
            int end = Math.min(start + size, total);
            List<Customer> pageRecords = start >= total ? Collections.emptyList() : records.subList(start, end);

            HashMap<String,Object> map = new HashMap<>();
            map.put("records", pageRecords);
            map.put("total", (long) total);
            map.put("pages", (total + size - 1) / size);
            map.put("current", page);
            return R.ok(map);
        } catch (Exception e) {
            log.error("客户列表查询失败", e);
            return R.fail("客户数据加载失败：" + e.getMessage());
        }
    }

    // ==================== 以下方法保持不变，完全兼容你的业务 ====================
    @GetMapping("/getById")
    public R<Customer> getById(@RequestParam @NotNull(message = "客户ID不能为空") Integer id) {
        try {
            Customer customer = customerService.getById(id);
            return customer == null ? R.notFound("客户信息不存在") : R.ok(customer);
        } catch (Exception e) {
            log.error("查询客户详情失败", e);
            return R.fail("查询失败");
        }
    }

    // 根据床位ID查询占用客户（供楼层总览使用）
    @GetMapping("/by-bed/{bedId}")
    public R<Customer> getByBedId(@PathVariable Integer bedId) {
        try {
            Customer customer = customerMapper.selectByBedId(bedId);
            return customer == null ? R.notFound("该床位暂无占用客户") : R.ok(customer);
        } catch (Exception e) {
            log.error("查询床位占用客户失败", e);
            return R.fail("查询失败");
        }
    }

    @PostMapping("/add")
    public R<String> add(@Valid @RequestBody Customer customer) {
        try {
            // ========== 0. 有效性验证 ==========
            if (customer.getName() == null || customer.getName().trim().isEmpty()) {
                return R.badRequest("客户姓名不能为空");
            }
            if (customer.getAge() != null && (customer.getAge() < 0 || customer.getAge() > 150)) {
                return R.badRequest("年龄必须在0-150之间");
            }

            // ========== 1. 护理级别映射：优先使用前端传入的 levelId，为空时再通过 nurseLevel 映射 ==========
            String nurseLevel = customer.getNurseLevel();
            if (customer.getLevelId() == null) {
                if (nurseLevel != null && !nurseLevel.isEmpty()) {
                    NursingLevel level = nursingLevelMapper.selectOne(
                        new LambdaQueryWrapper<NursingLevel>().eq(NursingLevel::getLevelName, nurseLevel)
                    );
                    if (level != null) {
                        customer.setLevelId(level.getId());
                    }
                }
            }

            // ========== 2. 新增客户（不分配床位，床位在入住时分配） ==========
            customer.setBedId(null);       // 待分配床位
            customer.setIsCheckIn(0);      // 未入住
            customer.setIsOuting(0);
            customerService.save(customer);

            operationLogger.log(getCurrentUserId(), getCurrentUserName(),
                    "新增客户", "客户-" + customer.getName(),
                    "护理级别：" + (nurseLevel != null ? nurseLevel : "未分配"));

            return R.ok("客户信息新增成功，请在【入住管理】中分配床位");
        } catch (Exception e) {
            log.error("新增客户失败", e);
            return R.fail("新增失败：" + e.getMessage());
        }
    }

    @PutMapping("/update")
    public R<String> update(@Valid @RequestBody Customer customer) {
        if (customer.getId() == null) {
            return R.badRequest("修改失败：客户ID不能为空");
        }
        try {
            // ========== 0. 有效性验证 ==========
            if (customer.getAge() != null && (customer.getAge() < 0 || customer.getAge() > 150)) {
                return R.badRequest("年龄必须在0-150之间");
            }

            // ========== 1. 护理级别映射：优先使用前端传入的 levelId，为空时再通过 nurseLevel 映射 ==========
            if (customer.getLevelId() == null) {
                String newNurseLevel = customer.getNurseLevel();
                if (newNurseLevel != null && !newNurseLevel.isEmpty()) {
                    NursingLevel level = nursingLevelMapper.selectOne(
                        new LambdaQueryWrapper<NursingLevel>().eq(NursingLevel::getLevelName, newNurseLevel)
                    );
                    if (level != null) {
                        customer.setLevelId(level.getId());
                    }
                }
            }

            // ========== 2. 更新客户（不处理床位，床位由入住/退住管理） ==========
            boolean success = customerService.updateById(customer);

            if (success) {
                operationLogger.log(getCurrentUserId(), getCurrentUserName(),
                        "编辑客户", "客户-" + customer.getName());
            }
            return success ? R.ok("客户信息修改成功") : R.notFound("修改失败：客户信息不存在");
        } catch (Exception e) {
            log.error("修改客户失败", e);
            return R.fail("修改失败：" + e.getMessage());
        }
    }

    /**
     * 更新床位状态（根据床位ID）
     */
    private void updateBedStatusById(Integer bedId, int status) {
        try {
            Bed bed = bedMapper.selectById(bedId);
            if (bed != null) {
                bed.setStatus(status);
                bedMapper.updateById(bed);
            }
        } catch (Exception e) {
            log.warn("更新床位状态失败，bedId={}, status={}", bedId, status, e);
        }
    }

    @DeleteMapping("/delete/{id}")
    public R<String> delete(@PathVariable @NotNull(message = "客户ID不能为空") Integer id) {
        try {
            // 1. 查询客户信息
            Customer customer = customerService.getById(id);
            if (customer == null) {
                return R.notFound("客户信息不存在");
            }

            // 2. 如果客户仍在住，要求先退住再删除
            if (customer.getIsCheckIn() != null && customer.getIsCheckIn() == 1) {
                return R.badRequest("该客户仍在住，请先办理退住后再删除");
            }

            // 3. 释放床位
            if (customer.getBedId() != null) {
                updateBedStatusById(customer.getBedId(), 0);
            }

            // 4. 删除关联的入住记录
            checkInRecordMapper.deleteByCustomerId(id);
            // 5. 删除客户
            boolean success = customerService.removeById(id);
            if (success) {
                operationLogger.log(getCurrentUserId(), getCurrentUserName(),
                        "删除客户", "客户-" + customer.getName());
            }
            return success ? R.ok("客户信息删除成功") : R.notFound("删除失败：客户信息不存在");
        } catch (Exception e) {
            log.error("删除客户失败，id={}", id, e);
            return R.fail("删除失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/batchDelete")
    public R<String> batchDelete(@RequestBody @NotNull(message = "ID列表不能为空") List<Integer> ids) {
        customerService.removeByIds(ids);
        return R.ok("批量删除成功，共删除" + ids.size() + "条记录");
    }

    @PostMapping("/import")
    public R<String> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), CustomerExcelDTO.class, new CustomerExcelListener(customerService))
                .sheet()
                .doRead();
        return R.ok("Excel导入成功");
    }

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("老人信息表", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        List<Customer> list = customerService.list();
        EasyExcel.write(response.getOutputStream(), CustomerExcelDTO.class)
                .sheet("老人信息")
                .doWrite(list);
    }

    /**
     * 查询所有在住客户（不分页）——供退住申请、入住管理等下拉框使用
     */
    @GetMapping("/checked-in")
    public R<List<Customer>> getCheckedInCustomers() {
        try {
            List<Customer> list = customerMapper.selectAllCheckedIn();

            // 批量查询待审核退住申请，标记到客户对象上
            if (!list.isEmpty()) {
                List<Integer> customerIds = list.stream()
                    .map(Customer::getId)
                    .collect(Collectors.toList());
                List<Backdown> pendingList = backdownService.lambdaQuery()
                    .eq(Backdown::getStatus, 0)
                    .in(Backdown::getCustomerId, customerIds)
                    .list();
                Map<Integer, Integer> backdownMap = pendingList.stream()
                    .collect(Collectors.toMap(Backdown::getCustomerId, Backdown::getId, (a, b) -> a));
                list.forEach(c -> c.setPendingBackdownId(backdownMap.get(c.getId())));
            }

            return R.ok(list);
        } catch (Exception e) {
            log.error("查询在住客户失败", e);
            return R.fail("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/search")
    public R<Map<String, Object>> searchCustomer(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Customer> result = customerService.searchCustomerPage(name, phone, tag, page, size);
            HashMap<String,Object> map = new HashMap<>();
            map.put("records", result.getRecords());
            map.put("total", result.getTotal());
            map.put("pages", result.getPages());
            map.put("current", result.getCurrent());
            return R.ok(map);
        } catch (Exception e) {
            log.error("客户搜索失败", e);
            return R.fail("搜索失败");
        }
    }

    // ==================== 操作日志辅助方法 ====================
    private Integer getCurrentUserId() {
        try {
            return (Integer) httpRequest.getAttribute("userId");
        } catch (Exception e) {
            return 0;
        }
    }
    private String getCurrentUserName() {
        try {
            return (String) httpRequest.getAttribute("username");
        } catch (Exception e) {
            return "未知";
        }
    }
}
