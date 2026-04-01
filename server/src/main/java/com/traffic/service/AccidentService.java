package com.traffic.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.traffic.dto.AccidentCreateRequest;
import com.traffic.dto.AccidentUpdateRequest;
import com.traffic.entity.AccidentRecord;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public interface AccidentService {

    IPage<AccidentRecord> pageAccidents(long pageNum, long pageSize, String roadName,
        String accidentType, LocalDateTime startTime, LocalDateTime endTime);

    AccidentRecord getById(Long id);

    void create(AccidentCreateRequest request);

    void update(Long id, AccidentUpdateRequest request);

    void delete(Long id);

    Map<String, Object> importCsv(MultipartFile file);
}
