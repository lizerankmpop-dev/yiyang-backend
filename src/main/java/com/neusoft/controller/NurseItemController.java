package com.neusoft.controller;

import com.neusoft.common.R;
import com.neusoft.entity.NurseItem;
import com.neusoft.entity.NursingLevelItem;
import com.neusoft.service.NurseItemService;
import com.neusoft.service.NursingLevelItemService;
import com.neusoft.utils.ValidateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/nurse-item")
@RequiredArgsConstructor // 修复：替换@Autowired为构造器注入
public class NurseItemController {

    private final NurseItemService nurseItemService;
    private final NursingLevelItemService nursingLevelItemService;

    @GetMapping("/list")
    public R<List<NurseItem>> list(@RequestParam(required = false) String category) {
        if (ValidateUtil.isBlank(category)) {
            return R.ok(nurseItemService.lambdaQuery().orderByAsc(NurseItem::getId).list());
        }
        return R.ok(nurseItemService.lambdaQuery()
                .eq(NurseItem::getCategory, ValidateUtil.trim(category))
                .orderByAsc(NurseItem::getId)
                .list());
    }

    /**
     * 根据护理级别获取推荐的护理项目
     */
    @GetMapping("/by-level/{levelId}")
    public R<List<NurseItem>> getItemsByLevel(@PathVariable Integer levelId) {
        if (!ValidateUtil.isPositive(levelId)) {
            return R.badRequest("护理级别ID无效");
        }
        List<NursingLevelItem> levelItems = nursingLevelItemService.lambdaQuery()
                .eq(NursingLevelItem::getLevelId, levelId)
                .list();
        if (levelItems.isEmpty()) {
            return R.ok(Collections.emptyList());
        }
        List<Integer> itemIds = levelItems.stream()
                .map(NursingLevelItem::getItemId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (itemIds.isEmpty()) {
            return R.ok(Collections.emptyList());
        }
        List<NurseItem> items = nurseItemService.lambdaQuery()
                .in(NurseItem::getId, itemIds)
                .eq(NurseItem::getStatus, 1)
                .list();
        return R.ok(items);
    }

    @PostMapping("/add")
    public R<String> add(@RequestBody NurseItem nurseItem) {
        R<String> error = validateNurseItem(nurseItem, false);
        if (error != null) {
            return error;
        }
        if (isItemNameRepeated(nurseItem)) {
            return R.fail("护理项目名称已存在");
        }
        nurseItem.setId(nextId());
        nurseItemService.save(nurseItem);
        return R.ok("新增成功");
    }

    @PutMapping("/update") // 修复：POST → PUT（符合RESTful规范）
    public R<String> update(@RequestBody NurseItem nurseItem) {
        R<String> error = validateNurseItem(nurseItem, true);
        if (error != null) {
            return error;
        }
        if (nurseItemService.getById(nurseItem.getId()) == null) {
            return R.fail("护理项目不存在");
        }
        if (isItemNameRepeated(nurseItem)) {
            return R.fail("护理项目名称已存在");
        }
        nurseItemService.updateById(nurseItem);
        return R.ok("修改成功");
    }

    @DeleteMapping("/delete") // 修复：GET → DELETE（符合RESTful规范）
    public R<String> delete(@RequestParam Integer id) {
        if (!ValidateUtil.isPositive(id)) {
            return R.fail("ID无效");
        }
        if (nurseItemService.getById(id) == null) {
            return R.fail("护理项目不存在");
        }
        // 修复：int → long（count()返回Long类型）
        long count = nursingLevelItemService.lambdaQuery()
                .eq(NursingLevelItem::getItemId, id)
                .count();
        if (count > 0) {
            return R.fail("该护理项目已配置到护理级别，不能删除");
        }
        nurseItemService.removeById(id);
        return R.ok("删除成功");
    }

    private R<String> validateNurseItem(NurseItem nurseItem, boolean requireId) {
        if (nurseItem == null) {
            return R.fail("护理项目信息不能为空");
        }
        if (requireId && !ValidateUtil.isPositive(nurseItem.getId())) {
            return R.fail("ID无效");
        }
        if (!ValidateUtil.lengthBetween(nurseItem.getItemName(), 1, 30)) {
            return R.fail("护理项目名称不能为空，且长度不能超过30个字符");
        }
        if (!ValidateUtil.lengthBetween(nurseItem.getCategory(), 1, 30)) {
            return R.fail("护理项目分类不能为空，且长度不能超过30个字符");
        }
        if (nurseItem.getPrice() == null || nurseItem.getPrice() < 0) {
            return R.fail("护理项目价格不能小于0");
        }
        if (nurseItem.getUnit() != null && nurseItem.getUnit().trim().length() > 20) {
            return R.fail("单位长度不能超过20个字符");
        }
        if (nurseItem.getDescription() != null && nurseItem.getDescription().trim().length() > 100) {
            return R.fail("说明长度不能超过100个字符");
        }
        nurseItem.setItemName(ValidateUtil.trim(nurseItem.getItemName()));
        nurseItem.setCategory(ValidateUtil.trim(nurseItem.getCategory()));
        nurseItem.setUnit(ValidateUtil.trim(nurseItem.getUnit()));
        nurseItem.setDescription(ValidateUtil.trim(nurseItem.getDescription()));
        return null;
    }

    private boolean isItemNameRepeated(NurseItem nurseItem) {
        List<NurseItem> list = nurseItemService.list();
        for (NurseItem item : list) {
            if (Objects.equals(ValidateUtil.trim(item.getItemName()), nurseItem.getItemName())
                    && !Objects.equals(item.getId(), nurseItem.getId())) {
                return true;
            }
        }
        return false;
    }

    private Integer nextId() {
        int maxId = 0;
        for (NurseItem item : nurseItemService.list()) {
            if (item.getId() != null && item.getId() > maxId) {
                maxId = item.getId();
            }
        }
        return maxId + 1;
    }
}