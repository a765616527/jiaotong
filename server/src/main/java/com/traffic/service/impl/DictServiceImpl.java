package com.traffic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.traffic.common.ApiException;
import com.traffic.dto.DictItemCreateRequest;
import com.traffic.dto.DictItemUpdateRequest;
import com.traffic.entity.DictItem;
import com.traffic.mapper.DictItemMapper;
import com.traffic.service.DictService;
import com.traffic.service.OperationLogService;
import com.traffic.util.SecurityUtils;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final DictItemMapper dictItemMapper;
    private final OperationLogService operationLogService;

    @Override
    public IPage<DictItem> pageAdmin(long pageNum, long pageSize, String dictType, Integer enabled) {
        SecurityUtils.requireAdmin();
        Page<DictItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DictItem> wrapper = new LambdaQueryWrapper<DictItem>()
            .orderByAsc(DictItem::getDictType)
            .orderByAsc(DictItem::getSortNo)
            .orderByAsc(DictItem::getId);
        if (dictType != null && !dictType.isBlank()) {
            wrapper.eq(DictItem::getDictType, dictType.trim().toUpperCase());
        }
        if (enabled != null) {
            wrapper.eq(DictItem::getEnabled, enabled);
        }
        return dictItemMapper.selectPage(page, wrapper);
    }

    @Override
    public List<DictItem> listItems(String dictType, Integer enabled) {
        LambdaQueryWrapper<DictItem> wrapper = new LambdaQueryWrapper<DictItem>()
            .orderByAsc(DictItem::getSortNo)
            .orderByAsc(DictItem::getId);
        if (dictType != null && !dictType.isBlank()) {
            wrapper.eq(DictItem::getDictType, dictType.trim().toUpperCase());
        }
        if (enabled != null) {
            wrapper.eq(DictItem::getEnabled, enabled);
        }
        return dictItemMapper.selectList(wrapper);
    }

    @Override
    public void create(DictItemCreateRequest request) {
        SecurityUtils.requireAdmin();
        String dictType = request.getDictType().trim().toUpperCase();
        String dictCode = request.getDictCode().trim().toUpperCase();
        ensureCodeUnique(dictType, dictCode, null);

        DictItem item = new DictItem();
        item.setDictType(dictType);
        item.setDictCode(dictCode);
        item.setDictName(request.getDictName().trim());
        item.setSortNo(request.getSortNo());
        item.setEnabled(request.getEnabled());
        item.setRemark(request.getRemark());
        item.setUpdatedBy(SecurityUtils.currentUserId());
        item.setUpdatedAt(LocalDateTime.now());
        dictItemMapper.insert(item);

        operationLogService.log("DICT", "CREATE", "DICT_ITEM", String.valueOf(item.getId()),
            "新增字典项: " + dictType + "/" + dictCode);
    }

    @Override
    public void update(Long id, DictItemUpdateRequest request) {
        SecurityUtils.requireAdmin();
        DictItem existed = getById(id);
        existed.setDictName(request.getDictName().trim());
        existed.setSortNo(request.getSortNo());
        existed.setEnabled(request.getEnabled());
        existed.setRemark(request.getRemark());
        existed.setUpdatedBy(SecurityUtils.currentUserId());
        existed.setUpdatedAt(LocalDateTime.now());
        dictItemMapper.updateById(existed);

        operationLogService.log("DICT", "UPDATE", "DICT_ITEM", String.valueOf(id),
            "更新字典项: " + existed.getDictType() + "/" + existed.getDictCode());
    }

    @Override
    public void delete(Long id) {
        SecurityUtils.requireAdmin();
        DictItem existed = getById(id);
        dictItemMapper.deleteById(id);
        operationLogService.log("DICT", "DELETE", "DICT_ITEM", String.valueOf(id),
            "删除字典项: " + existed.getDictType() + "/" + existed.getDictCode());
    }

    private DictItem getById(Long id) {
        DictItem existed = dictItemMapper.selectById(id);
        if (existed == null) {
            throw new ApiException(404, "字典项不存在");
        }
        return existed;
    }

    private void ensureCodeUnique(String dictType, String dictCode, Long ignoreId) {
        LambdaQueryWrapper<DictItem> wrapper = new LambdaQueryWrapper<DictItem>()
            .eq(DictItem::getDictType, dictType)
            .eq(DictItem::getDictCode, dictCode);
        if (ignoreId != null) {
            wrapper.ne(DictItem::getId, ignoreId);
        }
        Long count = dictItemMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new ApiException(400, "同类型下字典编码已存在");
        }
    }
}
