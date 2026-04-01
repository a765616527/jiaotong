package com.traffic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("warning_rule_config")
public class WarningRuleConfig {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleKey;
    private String ruleValue;
    private String description;
    private Long updatedBy;
    private LocalDateTime updatedAt;
}
