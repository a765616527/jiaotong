package com.traffic.controller;

import com.traffic.common.ApiResponse;
import com.traffic.entity.DictItem;
import com.traffic.service.DictService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dicts")
public class DictController {

    private final DictService dictService;

    @GetMapping("/items")
    public ApiResponse<List<DictItem>> listItems(
        @RequestParam String dictType,
        @RequestParam(defaultValue = "1") Integer enabled) {
        return ApiResponse.success(dictService.listItems(dictType, enabled));
    }
}
