package com.traffic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("warning_event")
public class WarningEvent {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String warningCode;
    private String riskLevel;
    private String targetRoad;
    private String targetTimeWindow;
    private String triggerReason;
    private String status;
    private LocalDateTime publishedAt;
    private LocalDateTime resolvedAt;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
