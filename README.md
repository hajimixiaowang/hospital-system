# 医院预约挂号与就诊系统

《软件设计模式》期末大作业 · 3人小组

| 成员 | 负责模块 | 设计模式（每人2种，组内不重复） | 关键类 |
|---|---|---|---|
| 刘嘉钊 | 挂号模块（号源查询、下单、计费） | **策略 + 工厂方法** | `FeeStrategy` / `FeeCalculator`；`RegistrationFactory` / `RegistrationProduct` |
| 陈梓健 | 就诊与通知模块（就诊流程、消息通知） | **观察者 + 模板方法** | `RegistrationEventPublisher` / 3个`Listener`；`VisitProcessTemplate` / 3个流程子类 |
| 冯冠丰 | 号源与基础模块（号源生成、排班、用户权限、科室管理） | **单例 + 建造者** | `NumberSourceGenerator`（DCL单例）；`SchedulePlan.Builder` |

技术栈：Spring Boot 2.7.18 + MyBatis-Plus 3.5.3 + MySQL 8 + Thymeleaf（纯手写CSS，无CDN依赖，离线可演示）

## 一、运行步骤

```bash
# 1. 建库（MySQL 8）：执行 sql/hospital.sql（含演示数据）

# 2. 改数据库密码：src/main/resources/application.yml 中 spring.datasource.password

# 3. 启动（JDK 8+，Maven 3.6+）
mvn spring-boot:run

# 4. 访问 http://localhost:8080
```

## 二、演示账号（密码均为 123456，管理员为 admin123）

| 角色 | 账号 | 说明 |
|---|---|---|
| 管理员 | admin | 科室/医生管理、排班放号、统计 |
| 医生 | zhangwei / lisi / wangfang / jiuzhen | 内科主任 / 外科副主任 / 儿科主治 / 急诊主治 |
| 患者 | p1 / p2 / p3 | 普通患者(全额) / 医保患者(8折) / 学生患者(5折) |

## 三、3~5 分钟演示脚本（拍视频照这个走）

1. **管理员登录**（30s）：科室管理看一眼 → 排班放号：选"内科·张伟"，明天~后天，上午+下午，每时段20，跳过周末 → 提示"放号成功，共生成 4 个号源"（讲：**建造者**链式组装+统一校验、**单例**生成唯一号源编号）。
2. **患者 p1 登录**（60s）：号源列表看"您的应付"¥40.00 → 挂号（普通）→ 通知中心出现"预约成功"站内信（讲：**观察者**，挂号代码零通知逻辑）→ 取消再重新挂一个 → 学生患者 p3 登录对比同一号源 ¥20.00（讲：**策略**按患者类型切换算法）。
3. **医生 zhangwei 登录**（60s）：今日预约列表 → 进入操作台：流程步骤条"签到→问诊→开方→完成"（讲：**模板方法**，普通门诊无检查环节）→ 签到 → 问诊 → 故意点"完成"？不行，先开方 → 填诊断处方提交 → 点完成 → 提示就诊完成。
4. **患者 p1 回来看**（30s）：就诊记录出现诊断处方；通知中心有"就诊完成"。
5. **急诊对比**（30s）：患者挂"急诊"类型 → 医生端列表急诊置顶（讲：工厂方法生产 EMERGENCY 产品 + 急诊流程模板 isPriority）。
6. **收尾**（30s）：管理员统计页看各科室挂号量/收入。

## 四、设计模式与类结构对应（答辩必背）

| 模式 | 角色 | 系统中的类 |
|---|---|---|
| 策略 | 抽象策略 | `FeeStrategy` |
| 策略 | 具体策略 | `NormalFeeStrategy` / `InsuranceFeeStrategy` / `StudentFeeStrategy` / `NightFeeStrategy` |
| 策略 | 上下文 | `FeeCalculator`（Spring 自动收集所有策略实现） |
| 工厂方法 | 抽象产品 | `RegistrationProduct` |
| 工厂方法 | 具体产品 | `NormalRegistration` / `ExpertRegistration` / `EmergencyRegistration` |
| 工厂方法 | 创建者（含模板下单流程） | `RegistrationFactory`（抽象） |
| 工厂方法 | 具体工厂 | `NormalRegistrationFactory` / `ExpertRegistrationFactory` / `EmergencyRegistrationFactory` |
| 模板方法 | 抽象模板 | `VisitProcessTemplate`（final advance / nextStep 定骨架） |
| 模板方法 | 具体模板 | `GeneralVisitProcess` / `SpecialistVisitProcess` / `EmergencyVisitProcess`（钩子 needExam） |
| 观察者 | 抽象观察者 | `RegistrationListener` |
| 观察者 | 具体观察者 | `InboxNotificationListener` / `RemindListener` / `SmsMockListener` |
| 观察者 | 主题 | `RegistrationEventPublisher`（广播）/ `RegistrationEvent`（事件） |
| 单例 | 唯一实例 | `NumberSourceGenerator`（双重检查锁 + volatile） |
| 建造者 | 产品 | `SchedulePlan` |
| 建造者 | 建造者 | `SchedulePlan.Builder`（链式 + build() 统一校验） |

**模式协作点（评分标准里的"模式融合"）**：
- 工厂 ↔ 策略：`RegistrationFactory.create()` 内调 `FeeCalculator.calcFee()` —— 造哪种单（工厂）与收多少钱（策略）解耦。
- 模板 ↔ 观察者：`VisitService.advance()` FINISH 环节发 `VISIT_FINISHED` 事件，RemindListener 通知患者 —— 流程模块无任何通知代码。
- 建造者 ↔ 单例：`ScheduleService.generateSchedules()` 里 `SchedulePlan.expand()` 展开明细后调 `NumberSourceGenerator.nextSourceNo()` 逐条取唯一编号。
- 挂号 ↔ 观察者：`RegistrationService.book()/cancel()` 只广播事件，站内信/模拟短信渠道各自监听。

## 五、七大设计原则落地位置（报告 2.1 节）

| 原则 | 落地位置 |
|---|---|
| 单一职责 | 计费（FeeCalculator）、下单（工厂）、通知（各Listener）、流程（模板）各自独立成类 |
| 开闭 | 新增计费策略 / 通知渠道 / 挂号类型 / 就诊流程变体 均为"只加不改" |
| 里氏代换 | 各策略/产品/监听器子类可在任何使用父类接口处替换，主流程无 instanceof 分支 |
| 依赖倒转 | RegistrationService 依赖 RegistrationFactory 抽象；FeeCalculator 依赖 FeeStrategy 接口 |
| 接口隔离 | FeeStrategy 仅一个 calculate；RegistrationListener 仅一个 onEvent，小而专 |
| 合成复用 | FeeCalculator 组合策略对象；VisitService 组合模板对象，全部用组合不用继承实现复用 |
| 迪米特 | 挂号服务不感知通知实现（只发事件）；建造者屏蔽号源组装细节 |

## 六、测试

```bash
mvn test
```

`src/test/java/com/hospital/PatternTests.java`：14 个纯 POJO 单测覆盖 4 种可脱离数据库验证的模式——
策略（普通/医保/学生/夜间四种计费数值断言）、单例（100线程取同一实例 + 16线程×50次并发无重号）、
建造者（链式构建展开数量 + 3种非法配置拒绝构建）、模板方法（流程顺序拒绝、开方必填诊断、三种模板步骤序列差异）。

## 七、常见答辩问题速答

1. **为什么用单例还要数据库唯一索引？** 单例解决的是"进程内编号不重 + 并发序号分配"，唯一索引兜底"多实例部署/重启丢内存序号"，双保险。
2. **为什么用 volatile？** 防止 new 的三步（分配内存/初始化/赋引用）被指令重排，避免别的线程拿到未初始化对象。
3. **新增一种挂号类型要改哪些文件？** 只加：一个产品类 + 一个工厂类 + RegistrationService 构造器注入（自动收集），公共下单流程零修改。
4. **模板方法里 final 的意义？** 锁死骨架，子类只能通过钩子（needExam/steps）注入差异，防止子类破坏流程纪律。
5. **Spring 事件和 GOF 观察者什么关系？** ApplicationEventPublisher 是观察者的框架化实现，我们的 Listener 接口保持 GOF 语义，@EventListener 只是接入点，换掉 Spring 也能用接口直接实现主题-观察者。
