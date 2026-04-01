package com.traffic.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.traffic.dto.DictItemCreateRequest;
import com.traffic.dto.DictItemUpdateRequest;
import com.traffic.entity.DictItem;
import java.util.List;

public interface DictService {

    IPage<DictItem> pageAdmin(long pageNum, long pageSize, String dictType, Integer enabled);

    List<DictItem> listItems(String dictType, Integer enabled);

    void create(DictItemCreateRequest request);

    void update(Long id, DictItemUpdateRequest request);

    void delete(Long id);
}
