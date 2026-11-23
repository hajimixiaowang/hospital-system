-- ============================================================
-- 医院预约挂号与就诊系统 数据库脚本（含演示数据）
-- MySQL 8.0，先执行本脚本建库建表
-- ============================================================
CREATE DATABASE IF NOT EXISTS hospital DEFAULT CHARSET utf8mb4;
USE hospital;

DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS announcement;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS visit_record;
DROP TABLE IF EXISTS registration;
DROP TABLE IF EXISTS schedule;
DROP TABLE IF EXISTS doctor;
DROP TABLE IF EXISTS department;
DROP TABLE IF EXISTS user;

-- 1. 用户表（患者/医生/管理员）
CREATE TABLE user (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  username      VARCHAR(50)  NOT NULL UNIQUE,
  password      VARCHAR(100) NOT NULL,                          -- SHA-256 摘要
  real_name     VARCHAR(50)  NOT NULL,
  phone         VARCHAR(20),
  role          VARCHAR(20)  NOT NULL DEFAULT 'PATIENT',       -- PATIENT/DOCTOR/ADMIN
  patient_type  VARCHAR(20)  DEFAULT 'NORMAL',                 -- NORMAL/INSURANCE/STUDENT
  status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',        -- ACTIVE/DISABLED（账号状态）
  create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- 2. 科室表（base_fee：计费策略因子一）
CREATE TABLE department (
  id        BIGINT PRIMARY KEY AUTO_INCREMENT,
  name      VARCHAR(50)     NOT NULL UNIQUE,
  base_fee  DECIMAL(10,2)   NOT NULL DEFAULT 10.00,
  intro     VARCHAR(500)
);

-- 3. 医生表（title_fee：计费策略因子二）
CREATE TABLE doctor (
  id        BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id   BIGINT NOT NULL,
  dept_id   BIGINT NOT NULL,
  title     VARCHAR(30)   NOT NULL,                             -- 主任医师/副主任医师/主治医师
  title_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  intro     VARCHAR(1000),                                      -- 医生简介
  good_at   VARCHAR(500),                                       -- 擅长领域
  FOREIGN KEY (user_id) REFERENCES user(id),
  FOREIGN KEY (dept_id) REFERENCES department(id)
);

-- 4. 号源表（source_no 由单例生成器保证唯一，唯一索引兜底）
CREATE TABLE schedule (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  source_no     VARCHAR(40) NOT NULL UNIQUE,
  doctor_id     BIGINT NOT NULL,
  work_date     DATE NOT NULL,
  time_slot     VARCHAR(20) NOT NULL,                           -- MORNING/AFTERNOON/NIGHT
  total_count   INT NOT NULL,
  remain_count  INT NOT NULL,
  status        VARCHAR(20) NOT NULL DEFAULT 'OPEN',            -- OPEN/CLOSED
  FOREIGN KEY (doctor_id) REFERENCES doctor(id)
);

-- 5. 挂号单表（reg_type 对应工厂方法产品族；fee 由策略模式计算）
CREATE TABLE registration (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  reg_no       VARCHAR(40) NOT NULL UNIQUE,
  reg_type     VARCHAR(20) NOT NULL,                            -- NORMAL/EXPERT/EMERGENCY
  patient_id   BIGINT NOT NULL,
  schedule_id  BIGINT NOT NULL,
  fee          DECIMAL(10,2) NOT NULL,
  status       VARCHAR(20) NOT NULL DEFAULT 'BOOKED',           -- BOOKED/PAID/CANCELLED/VISITED
  create_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (patient_id) REFERENCES user(id),
  FOREIGN KEY (schedule_id) REFERENCES schedule(id)
);

-- 6. 支付记录表（挂号后模拟支付）
CREATE TABLE payment (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  registration_id BIGINT NOT NULL UNIQUE,
  patient_id      BIGINT NOT NULL,
  amount          DECIMAL(10,2) NOT NULL,
  method          VARCHAR(20) NOT NULL DEFAULT 'BALANCE',       -- BALANCE/ALIPAY/WECHAT
  status          VARCHAR(20) NOT NULL DEFAULT 'PAID',          -- PAID/REFUNDED
  pay_time        DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (registration_id) REFERENCES registration(id),
  FOREIGN KEY (patient_id) REFERENCES user(id)
);

-- 7. 就诊记录表（各环节时间戳记录模板方法流程轨迹）
CREATE TABLE visit_record (
  id               BIGINT PRIMARY KEY AUTO_INCREMENT,
  registration_id  BIGINT NOT NULL UNIQUE,
  doctor_id        BIGINT NOT NULL,
  sign_in_time     DATETIME,
  inquiry_time     DATETIME,
  exam_time        DATETIME,
  prescribe_time   DATETIME,
  finish_time      DATETIME,
  diagnosis        VARCHAR(1000),
  prescription     VARCHAR(1000),
  FOREIGN KEY (registration_id) REFERENCES registration(id),
  FOREIGN KEY (doctor_id) REFERENCES doctor(id)
);

-- 8. 就诊评价表（就诊完成后患者打分）
CREATE TABLE review (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  registration_id BIGINT NOT NULL UNIQUE,
  patient_id      BIGINT NOT NULL,
  doctor_id       BIGINT NOT NULL,
  rating          INT NOT NULL,                                 -- 1~5 星
  content         VARCHAR(1000),
  create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (registration_id) REFERENCES registration(id),
  FOREIGN KEY (patient_id) REFERENCES user(id),
  FOREIGN KEY (doctor_id) REFERENCES doctor(id)
);

-- 9. 通知表（观察者模式的站内信落地）
CREATE TABLE notification (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT NOT NULL,
  type        VARCHAR(30) NOT NULL,                             -- BOOKED/CANCELLED/REMIND/VISIT_FINISHED
  title       VARCHAR(100),
  content     VARCHAR(500),
  status      VARCHAR(20) NOT NULL DEFAULT 'UNREAD',            -- UNREAD/READ
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 10. 公告表
CREATE TABLE announcement (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  title       VARCHAR(100) NOT NULL,
  content     VARCHAR(2000),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 11. 收藏表（患者收藏医生）
CREATE TABLE favorite (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id  BIGINT NOT NULL,
  doctor_id   BIGINT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_fav (patient_id, doctor_id),
  FOREIGN KEY (patient_id) REFERENCES user(id),
  FOREIGN KEY (doctor_id) REFERENCES doctor(id)
);

-- 12. AI 助手知识库表（检索式问答：管理员维护，关键词匹配作答）
CREATE TABLE knowledge (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  keywords    VARCHAR(500) NOT NULL,                            -- 命中关键词，逗号分隔
  answer      VARCHAR(2000) NOT NULL,                           -- 命中后的回答
  category    VARCHAR(30) DEFAULT '通用',                       -- 分类：挂号/费用/就诊/通用
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 13. 系统设置表（键值对）
CREATE TABLE setting (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  setting_key  VARCHAR(50) NOT NULL UNIQUE,                     -- 如 hospital_name
  setting_value VARCHAR(200) NOT NULL,
  remark       VARCHAR(200)
);

-- ============================================================
-- 演示数据
-- 密码均为 123456（admin 为 admin123），SHA-256 后入库
-- ============================================================

-- 账号：
--   管理员 admin / admin123
--   医生   zhangwei(内科·主任医师张伟) / lisi(外科·副主任医师李强) / wangfang(儿科·主治医师王芳)
--          / jiuzhen(急诊·主治医师陈急)，密码均为 123456
--   患者   p1(普通) / p2(医保) / p3(学生)，密码 123456
INSERT INTO user (id, username, password, real_name, phone, role, patient_type, status) VALUES
(1, 'admin',     '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '系统管理员', '13800000000', 'ADMIN',    NULL, 'ACTIVE'),
(2, 'zhangwei',  '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '张伟', '13811112222', 'DOCTOR', NULL, 'ACTIVE'),
(3, 'lisi',      '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '李强', '13833334444', 'DOCTOR', NULL, 'ACTIVE'),
(4, 'wangfang',  '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '王芳', '13855556666', 'DOCTOR', NULL, 'ACTIVE'),
(5, 'jiuzhen',   '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '陈急', '13877778888', 'DOCTOR', NULL, 'ACTIVE'),
(6, 'p1',        '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '赵小明', '13900000001', 'PATIENT', 'NORMAL', 'ACTIVE'),
(7, 'p2',        '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '钱医保', '13900000002', 'PATIENT', 'INSURANCE', 'ACTIVE'),
(8, 'p3',        '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '孙学生', '13900000003', 'PATIENT', 'STUDENT', 'ACTIVE');

INSERT INTO department (id, name, base_fee, intro) VALUES
(1, '内科', 10.00, '呼吸、消化、心血管等内科常见病诊治'),
(2, '外科', 15.00, '普外、骨科、创伤修复等手术治疗'),
(3, '儿科', 12.00, '0~14岁儿童常见病、多发病诊治'),
(4, '急诊', 20.00, '24小时急危重症接诊，优先处置');

INSERT INTO doctor (id, user_id, dept_id, title, title_fee, intro, good_at) VALUES
(1, 2, 1, '主任医师',   30.00, '从事内科临床工作 30 余年，博士生导师，学科带头人。', '高血压、冠心病、慢性胃炎'),
(2, 3, 2, '副主任医师', 20.00, '外科微创手术方向资深专家，累计完成手术 5000 余例。', '腹腔镜微创、疝气修复、创伤骨科'),
(3, 4, 3, '主治医师',   10.00, '儿科常见病诊治经验丰富，亲和力强，深受家长信赖。', '小儿呼吸道感染、腹泻、生长发育评估'),
(4, 5, 4, '主治医师',   10.00, '急诊一线值守医生，处置急危重症经验丰富。', '急性胸痛、创伤急救、心肺复苏');

-- 号源：今明两天各时段有号（work_date 用 CURRENT_DATE 偏移，任何时间执行都有号可约）
INSERT INTO schedule (source_no, doctor_id, work_date, time_slot, total_count, remain_count, status) VALUES
('NS-DEMO-0001', 1, CURRENT_DATE() + INTERVAL 0 DAY, 'MORNING',   20, 20, 'OPEN'),
('NS-DEMO-0002', 1, CURRENT_DATE() + INTERVAL 0 DAY, 'AFTERNOON', 20, 20, 'OPEN'),
('NS-DEMO-0003', 1, CURRENT_DATE() + INTERVAL 1 DAY, 'MORNING',   20, 20, 'OPEN'),
('NS-DEMO-0004', 1, CURRENT_DATE() + INTERVAL 1 DAY, 'NIGHT',     10, 10, 'OPEN'),
('NS-DEMO-0005', 2, CURRENT_DATE() + INTERVAL 0 DAY, 'MORNING',   15, 15, 'OPEN'),
('NS-DEMO-0006', 2, CURRENT_DATE() + INTERVAL 1 DAY, 'AFTERNOON', 15, 15, 'OPEN'),
('NS-DEMO-0007', 3, CURRENT_DATE() + INTERVAL 0 DAY, 'AFTERNOON', 25, 25, 'OPEN'),
('NS-DEMO-0008', 3, CURRENT_DATE() + INTERVAL 1 DAY, 'MORNING',   25, 25, 'OPEN'),
('NS-DEMO-0009', 4, CURRENT_DATE() + INTERVAL 0 DAY, 'NIGHT',     12, 12, 'OPEN');

INSERT INTO announcement (title, content) VALUES
('关于优化门诊预约挂号服务的通知', '为改善就医体验，我院已全面开通线上预约挂号，支持普通门诊、专家门诊与急诊三种类型。请患者朋友提前预约，按号就诊。'),
('医保患者挂号费结算政策说明', '医保患者在线上预约挂号时，系统将自动按 8 折结算挂号费；学生患者凭有效学生证可享 5 折优惠，请提前完善患者类型信息。'),
('夜间门诊开诊公告', '为方便上班族就诊，我院内科、急诊开设夜间门诊（18:00-21:00），夜间时段挂号费在上浮 20% 的基础上执行，请合理安排就诊时间。');

-- AI 助手知识库种子（关键词 -> 回答，管理端可维护）
INSERT INTO knowledge (keywords, answer, category) VALUES
('挂号,预约,怎么挂号,如何预约', '您好！预约挂号流程：1.登录后进入"预约挂号"；2.选择医生查看可约排班；3.选择号源与挂号类型（普通/专家/急诊）提交；4.在"我的订单"中完成支付即预约成功。就诊当天请提前15分钟到院签到。', '挂号'),
('退号,取消预约,退款,退挂号', '您好！就诊前可在"我的订单"中取消预约，号源立即释放；已支付的订单取消后挂号费将自动原路退款（1~3个工作日到账）。已就诊的订单无法取消。', '挂号'),
('费用,多少钱,挂号费,价格', '挂号费 = 科室基础费 + 医生职称附加费，再按患者类型结算：普通患者全额、医保患者8折、学生患者5折；夜间时段（18:00-21:00）统一上浮20%。下单前页面会显示您的实时应付金额。', '费用'),
('医保,医保报销,社保', '医保患者挂号费自动按8折结算，请先在"个人中心-个人资料"中把患者类型设为"医保患者"。挂号费发票可凭就诊记录到收费窗口补打。', '费用'),
('学生,学生证,学生优惠', '学生患者凭有效学生证可享挂号费5折优惠。请先在"个人中心-个人资料"中把患者类型设为"学生患者"，系统结算时自动打折。', '费用'),
('夜间,晚上,夜诊', '我院内科、急诊开设夜间门诊（18:00-21:00）。夜间时段挂号费统一上浮20%。夜间看病请直接在"预约挂号"中选择夜间时段号源。', '就诊'),
('急诊,急救,紧急', '急诊科24小时接诊，支持线上预约急诊号（优先处置）。如遇突发危重情况，请立即拨打120或直接前往急诊科，不要等待线上预约。', '就诊'),
('感冒,发烧,头疼,看什么科,推荐科室', '感冒、发烧、头疼等常见症状建议挂内科；儿童（0~14岁）建议挂儿科；外伤、骨折建议挂外科；症状危重请挂急诊。您可在"医生科室"中按科室浏览医生并挂号。', '就诊'),
('时间,几点,上班,开诊', '门诊开诊时间：上午 8:00-12:00，下午 14:00-17:30，夜间门诊 18:00-21:00。请在号源对应时段开始前15分钟到院签到候诊。', '就诊'),
('地址,位置,在哪,怎么去', '我院位于健康路1号（详见登录页页脚），可乘 3 路/18 路公交到"医院站"下车。停车场位于门诊楼负一层，就诊可免费停车2小时。', '通用'),
('签到,报到,候诊,取号', '就诊当天请在对应时段开始前15分钟，到科室分诊台出示挂号单号完成签到，系统按签到顺序候诊（急诊优先处置）。', '就诊');

-- 系统设置种子
INSERT INTO setting (setting_key, setting_value, remark) VALUES
('hospital_name', '仁和医院', '医院名称（显示在页面品牌区）'),
('service_phone', '080-1234-5678', '客服电话'),
('reg_advance_days', '7', '可提前预约天数'),
('hospital_address', '健康路1号（门诊楼）', '医院地址');
