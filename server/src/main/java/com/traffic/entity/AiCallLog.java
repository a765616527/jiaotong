package com.traffic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ai_call_log")
public class AiCallLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long operatorId;
    private String endpoint;
    private Integer success;
    private String errorMessage;
    private Integer inputLength;
    private Integer outputLength;
    private LocalDateTime calledAt;
}
