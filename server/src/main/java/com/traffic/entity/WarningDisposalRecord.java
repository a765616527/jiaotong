package com.traffic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("warning_disposal_record")
public class WarningDisposalRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long warningId;
    private String actionType;
    private String actionDesc;
    private Long operatorId;
    private LocalDateTime operatedAt;
}
