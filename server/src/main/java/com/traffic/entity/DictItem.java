package com.traffic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("dict_item")
public class DictItem {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String dictType;
    private String dictCode;
    private String dictName;
    private Integer sortNo;
    private Integer enabled;
    private String remark;
    private Long updatedBy;
    private LocalDateTime updatedAt;
}
