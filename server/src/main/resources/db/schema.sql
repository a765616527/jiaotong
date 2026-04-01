CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  display_name VARCHAR(64) NOT NULL,
  role_code VARCHAR(32) NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS accident_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  occur_time DATETIME NOT NULL,
  road_name VARCHAR(128) NOT NULL,
  area_code VARCHAR(32),
  longitude DECIMAL(10,6) NOT NULL,
  latitude DECIMAL(10,6) NOT NULL,
  accident_type VARCHAR(64) NOT NULL,
  vehicle_count INT NOT NULL,
  casualty_count INT NOT NULL,
  description VARCHAR(500),
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_accident_time ON accident_record(occur_time);
CREATE INDEX idx_accident_road ON accident_record(road_name);

CREATE TABLE IF NOT EXISTS warning_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  warning_code VARCHAR(64) NOT NULL UNIQUE,
  risk_level VARCHAR(16) NOT NULL,
  target_road VARCHAR(128) NOT NULL,
  target_time_window VARCHAR(32),
  trigger_reason VARCHAR(500) NOT NULL,
  status VARCHAR(16) NOT NULL,
  published_at DATETIME,
  resolved_at DATETIME,
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE INDEX idx_warning_status ON warning_event(status);
CREATE INDEX idx_warning_road ON warning_event(target_road);

CREATE TABLE IF NOT EXISTS warning_disposal_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  warning_id BIGINT NOT NULL,
  action_type VARCHAR(32) NOT NULL,
  action_desc VARCHAR(500) NOT NULL,
  operator_id BIGINT NOT NULL,
  operated_at DATETIME NOT NULL
);

CREATE INDEX idx_disposal_warning ON warning_disposal_record(warning_id);

CREATE TABLE IF NOT EXISTS warning_rule_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  rule_key VARCHAR(64) NOT NULL UNIQUE,
  rule_value VARCHAR(64) NOT NULL,
  description VARCHAR(255),
  updated_by BIGINT NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS sys_operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  operator_id BIGINT NOT NULL,
  module VARCHAR(64) NOT NULL,
  action VARCHAR(64) NOT NULL,
  target_type VARCHAR(64) NOT NULL,
  target_id VARCHAR(64),
  detail VARCHAR(500),
  operated_at DATETIME NOT NULL
);

CREATE INDEX idx_log_module ON sys_operation_log(module);
CREATE INDEX idx_log_time ON sys_operation_log(operated_at);

CREATE TABLE IF NOT EXISTS ai_call_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  operator_id BIGINT NOT NULL,
  endpoint VARCHAR(64) NOT NULL,
  success TINYINT NOT NULL,
  error_message VARCHAR(500),
  input_length INT NOT NULL DEFAULT 0,
  output_length INT NOT NULL DEFAULT 0,
  called_at DATETIME NOT NULL
);

CREATE INDEX idx_ai_endpoint ON ai_call_log(endpoint);
CREATE INDEX idx_ai_time ON ai_call_log(called_at);

CREATE TABLE IF NOT EXISTS rule_change_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  rule_key VARCHAR(64) NOT NULL,
  old_value VARCHAR(64),
  new_value VARCHAR(64),
  operator_id BIGINT NOT NULL,
  note VARCHAR(255),
  changed_at DATETIME NOT NULL
);

CREATE INDEX idx_rule_change_key ON rule_change_log(rule_key);
CREATE INDEX idx_rule_change_time ON rule_change_log(changed_at);

CREATE TABLE IF NOT EXISTS dict_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dict_type VARCHAR(64) NOT NULL,
  dict_code VARCHAR(64) NOT NULL,
  dict_name VARCHAR(64) NOT NULL,
  sort_no INT NOT NULL DEFAULT 100,
  enabled TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(255),
  updated_by BIGINT NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE UNIQUE INDEX uk_dict_type_code ON dict_item(dict_type, dict_code);
CREATE INDEX idx_dict_type_enabled ON dict_item(dict_type, enabled);
