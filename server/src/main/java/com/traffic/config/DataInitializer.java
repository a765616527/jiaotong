package com.traffic.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.traffic.entity.DictItem;
import com.traffic.entity.SysUser;
import com.traffic.entity.WarningRuleConfig;
import com.traffic.mapper.DictItemMapper;
import com.traffic.mapper.SysUserMapper;
import com.traffic.mapper.WarningRuleConfigMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final WarningRuleConfigMapper ruleConfigMapper;
    private final DictItemMapper dictItemMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initAdminUser();
        initRuleConfigs();
        initDictItems();
    }

    private void initAdminUser() {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUsername, "admin"));
        if (count != null && count > 0) {
            return;
        }
        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("Admin@123456"));
        admin.setDisplayName("系统管理员");
        admin.setRoleCode("ROLE_ADMIN");
        admin.setEnabled(1);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(admin);
    }

    private void initRuleConfigs() {
        Map<String, String> defaults = Map.of(
            "W_FREQ_7D", "0.45",
            "W_FREQ_30D", "0.25",
            "W_NIGHT", "0.15",
            "W_SEVERE", "0.15",
            "THRESHOLD_MEDIUM", "40",
            "THRESHOLD_HIGH", "70"
        );
        defaults.forEach((k, v) -> {
            Long count = ruleConfigMapper.selectCount(new LambdaQueryWrapper<WarningRuleConfig>()
                .eq(WarningRuleConfig::getRuleKey, k));
            if (count != null && count > 0) {
                return;
            }
            WarningRuleConfig item = new WarningRuleConfig();
            item.setRuleKey(k);
            item.setRuleValue(v);
            item.setDescription("默认规则");
            item.setUpdatedBy(0L);
            item.setUpdatedAt(LocalDateTime.now());
            ruleConfigMapper.insert(item);
        });
    }

    private void initDictItems() {
        initDictType("ACCIDENT_TYPE", List.of(
            Map.entry("REAR_END", "追尾"),
            Map.entry("SCRAPE", "剐蹭"),
            Map.entry("SCRATCH", "刮擦"),
            Map.entry("COLLISION", "碰撞"),
            Map.entry("SIDE_COLLISION", "侧面碰撞"),
            Map.entry("PEDESTRIAN", "行人事故"),
            Map.entry("ROLL_OVER", "侧翻")));
        initDictType("ROAD_TYPE", List.of(
            Map.entry("URBAN_MAIN", "城市主干道"),
            Map.entry("URBAN_SECONDARY", "城市次干道"),
            Map.entry("INTERSECTION", "路口"),
            Map.entry("EXPRESSWAY", "快速路")));
        initDictType("WARNING_LEVEL", List.of(
            Map.entry("LOW", "低风险"),
            Map.entry("MEDIUM", "中风险"),
            Map.entry("HIGH", "高风险")));
    }

    private void initDictType(String dictType, List<Map.Entry<String, String>> rows) {
        for (int i = 0; i < rows.size(); i++) {
            Map.Entry<String, String> row = rows.get(i);
            String code = row.getKey();
            Long count = dictItemMapper.selectCount(new LambdaQueryWrapper<DictItem>()
                .eq(DictItem::getDictType, dictType)
                .eq(DictItem::getDictCode, code));
            if (count != null && count > 0) {
                continue;
            }
            DictItem item = new DictItem();
            item.setDictType(dictType);
            item.setDictCode(code);
            item.setDictName(row.getValue());
            item.setSortNo((i + 1) * 10);
            item.setEnabled(1);
            item.setRemark("系统初始化");
            item.setUpdatedBy(0L);
            item.setUpdatedAt(LocalDateTime.now());
            dictItemMapper.insert(item);
        }
    }
}
