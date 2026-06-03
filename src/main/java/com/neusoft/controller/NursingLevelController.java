package com.neusoft.controller;

import com.neusoft.common.R;
import com.neusoft.entity.CustomerNursingLevel;
import com.neusoft.entity.NursingLevel;
import com.neusoft.service.CustomerNursingLevelService;
import com.neusoft.service.NurseItemService;
import com.neusoft.service.NursingLevelService;
import com.neusoft.utils.ValidateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/nursing-level")
@RequiredArgsConstructor // 替换@Autowired为构造器注入
public class NursingLevelController {

    private final NursingLevelService nursingLevelService;
    private final NurseItemService nurseItemService;
    private final CustomerNursingLevelService customerNursingLevelService;

    @GetMapping("/list")
    public R<List<NursingLevel>> list() {
        List<NursingLevel> levels = nursingLevelService.lambdaQuery()
                .orderByAsc(NursingLevel::getId)
                .list();
        for (NursingLevel level : levels) {
            level.setItemIds(nursingLevelService.getItemIds(level.getId()));
        }
        return R.ok(levels);
    }

    @PostMapping("/add")
    public R<String> add(@RequestBody NursingLevel nursingLevel) {
        R<String> error = validateNursingLevel(nursingLevel, false);
        if (error != null) {
            return error;
        }
        if (isLevelNameRepeated(nursingLevel)) {
            return R.fail("护理级别名称已存在");
        }
        nursingLevelService.save(nursingLevel);
        nursingLevelService.saveLevelItems(nursingLevel.getId(), nursingLevel.getItemIds());
        return R.ok("新增成功");
    }

    @PutMapping("/update") // 修复：POST → PUT
    public R<String> update(@RequestBody NursingLevel nursingLevel) {
        R<String> error = validateNursingLevel(nursingLevel, true);
        if (error != null) {
            return error;
        }
        if (nursingLevelService.getById(nursingLevel.getId()) == null) {
            return R.fail("护理级别不存在");
        }
        if (isLevelNameRepeated(nursingLevel)) {
            return R.fail("护理级别名称已存在");
        }
        nursingLevelService.updateById(nursingLevel);
        nursingLevelService.saveLevelItems(nursingLevel.getId(), nursingLevel.getItemIds());
        return R.ok("修改成功");
    }

    @DeleteMapping("/delete") // 修复：GET → DELETE
    public R<String> delete(@RequestParam Integer id) {
        if (!ValidateUtil.isPositive(id)) {
            return R.fail("ID无效");
        }
        if (nursingLevelService.getById(id) == null) {
            return R.fail("护理级别不存在");
        }
        // ✅ 修复：int → long（count()返回Long类型）
        long count = customerNursingLevelService.lambdaQuery()
                .eq(CustomerNursingLevel::getLevelId, id)
                .count();
        if (count > 0) {
            return R.fail("该护理级别已分配给老人，不能删除");
        }
        nursingLevelService.removeById(id);
        nursingLevelService.saveLevelItems(id, null);
        return R.ok("删除成功");
    }

    private R<String> validateNursingLevel(NursingLevel nursingLevel, boolean requireId) {
        if (nursingLevel == null) {
            return R.fail("护理级别信息不能为空");
        }
        if (requireId && !ValidateUtil.isPositive(nursingLevel.getId())) {
            return R.fail("ID无效");
        }
        if (!ValidateUtil.lengthBetween(nursingLevel.getLevelName(), 1, 30)) {
            return R.fail("护理级别名称不能为空，且长度不能超过30个字符");
        }
        if (nursingLevel.getDescription() != null && nursingLevel.getDescription().trim().length() > 100) {
            return R.fail("说明长度不能超过100个字符");
        }
        if (nursingLevel.getItemIds() != null) {
            for (Integer itemId : nursingLevel.getItemIds()) {
                if (!ValidateUtil.isPositive(itemId)) {
                    return R.fail("护理项目ID无效");
                }
                if (nurseItemService.getById(itemId) == null) {
                    return R.fail("护理项目ID不存在：" + itemId);
                }
            }
        }
        nursingLevel.setLevelName(ValidateUtil.trim(nursingLevel.getLevelName()));
        nursingLevel.setDescription(ValidateUtil.trim(nursingLevel.getDescription()));
        return null;
    }

    private boolean isLevelNameRepeated(NursingLevel nursingLevel) {
        List<NursingLevel> list = nursingLevelService.list();
        for (NursingLevel item : list) {
            if (Objects.equals(ValidateUtil.trim(item.getLevelName()), nursingLevel.getLevelName())
                    && !Objects.equals(item.getId(), nursingLevel.getId())) {
                return true;
            }
        }
        return false;
    }
}