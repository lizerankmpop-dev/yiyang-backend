package com.neusoft.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class CustomerExcelDTO {
    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("年龄")
    private Integer age;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("标签")
    private String tags;
}