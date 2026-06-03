package com.neusoft.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.neusoft.dto.CustomerExcelDTO;
import com.neusoft.entity.Customer;
import com.neusoft.service.CustomerService;
import org.springframework.beans.BeanUtils;
import java.util.ArrayList;
import java.util.List;

public class CustomerExcelListener extends AnalysisEventListener<CustomerExcelDTO> {

    private final CustomerService customerService;
    private final List<Customer> list = new ArrayList<>();

    public CustomerExcelListener(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public void invoke(CustomerExcelDTO dto, AnalysisContext context) {
        Customer customer = new Customer();
        BeanUtils.copyProperties(dto, customer);
        list.add(customer);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 批量保存
        customerService.saveBatch(list);
    }
}