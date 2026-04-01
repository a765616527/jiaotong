package com.traffic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("accident_record")
public class AccidentRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDateTime occurTime;
    private String roadName;
    private String areaCode;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String accidentType;
    private Integer vehicleCount;
    private Integer casualtyCount;
    private String description;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
