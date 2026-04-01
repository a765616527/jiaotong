package com.traffic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("rule_change_log")
public class RuleChangeLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleKey;
    private String oldValue;
    private String newValue;
    private Long operatorId;
    private String note;
    private LocalDateTime changedAt;
}
