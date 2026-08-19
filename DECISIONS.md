# DECISIONS

本文件记录 Demo 实现中对策略留白、工程边界和 LLM Provider 的主要取舍。`src/main/resources/demo-data/policy.md` 仍是策略语义的唯一事实来源；本文件解释代码如何处理策略未完全写死的部分。

## 决策：策略优先级

- 冻结级风险优先级最高，命中制裁地址或风险分达到冻结阈值时输出 `FREEZE`。
- 合规阻断优先于运维复核和重新报价。
- 资金或数据异常不自动放行，进入 `OPS_REVIEW`、`TEMPORARY_HOLD` 或 `REQUOTE`。
- 只有所有强制规则通过、幂等门通过、执行门通过时，订单才可能执行资金动作。

理由：`policy.md` 的铁律要求命中合规不能自动放行，也不能释放未真实收到并确认的资金。

## 决策：保守失败

- 地址查不到、类别为 `unknown`、资产网络不支持、到账网络不一致等情况都不自行推断。
- 事实缺失不会被客户备注、模型输出或默认值补齐。
- 无法证明可安全放行时，输出人工复核或合规挂起。

理由：异常队列里的订单本来就是高风险路径，保守处理比错误放行更符合题意。

## 决策：Demo 事实

- Demo 事实直接放在仓库内，便于离线验收和测试复现。
- `customers.json`、`assets.json`、`address_risk.json`、`reference_rates.json` 是权威事实输入。
- `orders.jsonl` 是批处理样例输入，不作为规则定义来源。
- `policy.md` 中没有的信息不从客户备注或模型输出里推断。

## 决策：时间

- 全链路使用 UTC。
- Demo 评估时钟固定为 `2026-07-28T12:00:00Z`。
- 报价过期、滑点判断和审计时间都以该固定时钟为基准。

理由：固定时钟能让 Golden 样例和测试稳定，不受本地机器时间影响。

## 决策：金额

- 金额、汇率、滑点和 USD 估值统一使用 `BigDecimal`。
- 不使用 `double` 或 `float` 参与资金判断。
- off-ramp 出款金额不能超过已确认入金折算价值。

理由：资金系统不能接受二进制浮点带来的舍入误差和不可解释差异。

## 决策：规则计划

- `StaticRulePlanResolver` 显式声明每种订单类型适用的规则。
- on-ramp、off-ramp、withdrawal 使用不同的事实计划。
- LLM 不生成规则计划，不决定应调用哪些资金规则。

理由：规则适用性属于策略代码，不属于模型自由规划空间。

## 决策：最小金额

- 低于 `assets.json` 最小金额的订单不会自动完成。
- 当前 Demo 将其作为运维复核问题处理。

理由：`policy.md` 说明该行为留白；最小金额通常涉及链上成本、退款路径和产品规则，Demo 不擅自执行资金动作。

## 决策：KYC

- 当前 Demo 只检查单笔金额是否超过客户 KYC 档位月度限额。
- 不实现月度累计池。
- KYC 判断统一折算到 USD 口径。

理由：题目数据没有提供月度累计交易历史。对缺失历史做累计推断会制造不存在的事实。

## 决策：Travel Rule

- 只有明确 `is_vasp=true` 且转账 USD 等值达到 1000 及以上时才强制触发 Travel Rule 信息检查。
- VASP 名称为 `unknown` 仅作为警告或证据，不自行推断为已满足信息要求。
- 缺少受益人信息时输出合规挂起。

理由：Travel Rule 需要明确的发起方/受益方信息，不能由模型或备注补齐。

## 决策：幂等

- 订单级幂等门先于资金动作。
- off-ramp 的 `COMPLETE` 候选还必须通过交易级幂等门。
- 重复订单和重复交易只产生结构化拒绝或复核结果，不产生第二次资金动作。
- Demo 使用内存级幂等存储，生产环境应替换为持久化且具备并发保护的存储。

理由：`policy.md` 明确每笔链上到账或 webhook 只能处理一次。

## 决策：动作边界

- `ActionExecutor` 只读取确定性决策和幂等门结果。
- `ActionExecutor` 不读取 LLM 输出。
- 任何非 `COMPLETE` 决策都不会执行资金动作。

理由：资金授权必须来自可审计的确定性规则和执行门。

## 决策：审计和输出

- `results.json` 保存每单执行结果，便于 Demo 检查。
- `audit.jsonl` 保存逐行审计轨迹，便于追加写和流式消费。
- 审计中的 `decision`、`reasons`、`actionExecuted` 来自确定性策略，不来自 LLM。
- 审计允许记录 `explanationProvider`、`explanationFallbackUsed`、`explanationFailureCode` 这类非资金元数据。
- 不写入 API Key、Authorization header 或完整模型请求。

## 决策：LLM 可选

- 本项目的 Agent 不以 LLM 作为资金决策器。
- LLM 只负责把 `DeterministicDecision` 转换成更易读的解释文本。
- LLM 输出不能设置或修改 `Decision`、`ReasonCode`、`EscalationTarget`、`Retryability` 或资金动作。

理由：题目要求模型可替换和离线可跑，不要求把合规和资金授权交给模型。

## 决策：默认解释器是 Stub

- 不配置 `LLM_PROVIDER` 时默认 `STUB`。
- 默认路径不读取 `OPENAI_API_KEY`。
- 默认路径不访问公网。
- 默认路径必须能完整运行 14 单并产出结果、审计和解释文本。

理由：题目明确要求没有付费 Key 也能跑。

## 决策：解释器选择

- `LLM_PROVIDER=stub` 使用确定性离线解释。
- `LLM_PROVIDER=recorded` 使用本地录制回放，未命中时回退到 `stub`。
- `LLM_PROVIDER=openai` 才构造 OpenAI 解释器边界。
- 未知 provider 明确报配置错误，不静默切换。

理由：provider 选择必须显式、可审计，避免环境配置错误导致意外联网或行为漂移。

## 决策：配置失败

- `LLM_PROVIDER=openai` 但缺少 `OPENAI_API_KEY` 时明确配置失败。
- `LLM_PROVIDER=stub` 或 `recorded` 时，即使环境中没有 `OPENAI_API_KEY` 也不失败。
- `OPENAI_MODEL` 只在 OpenAI 模式使用。

理由：显式要求真实 provider 却缺少凭证是配置错误；默认离线路径不能被真实 provider 的配置要求拖垮。

## 决策：运行时失败

- 真实解释器运行时失败时回退到 `stub`。
- 解释器失败不改变 `Decision`、`ReasonCode`、`EscalationTarget`、`Retryability` 或 `Action`。
- Demo 使用 `SafeExplanationProvider` 处理运行时故障。
- 熔断器是生产演进点；当前 Demo 保留超时和边界配置，但不实现完整熔断状态机。

理由：解释文本不是资金主链必要事实，模型故障不应导致订单状态变化。

## 决策：录制回放解释器

- 录制回放键由 `Decision + sorted ReasonCodes` 组成。
- 录制命中返回本地文本。
- 录制未命中回退到 `stub`，并在解释元数据中标记 fallback。

理由：回放键不能依赖随机请求号、时间戳或客户自由文本，否则无法稳定复现。

## 决策：真实解释器边界

- 当前 Demo 不引入真实 OpenAI SDK。
- `OpenAiExplanationProvider` 是隔离边界，生产可在该类或其子包中接入真实 SDK。
- 真实 SDK 禁止泄漏到 `domain`、`policy`、`idempotency`、`runtime/ActionExecutor`。
- 模型输入应只包含确定性决策、原因码和必要的已验证事实摘要。

理由：保持业务层只依赖项目自有 `ExplanationProvider` 接口，降低供应商切换成本。

## 决策：LangChain4j

- 当前实现没有引入 LangChain4j。
- 原因不是不支持 LangChain4j，而是本阶段只需要一个自有解释器接口、离线 stub、录制回放和可选真实 provider 边界。
- 在没有工具规划、向量检索、对话记忆、链式调用等需求时，引入 LangChain4j 会增加依赖和测试面，却不提高资金决策正确性。
- 未来如果需要真实 OpenAI 调用，可以在 `infrastructure/llm/OpenAiExplanationProvider` 内部用 LangChain4j 实现，接口和业务层不需要变化。

理由：框架选择应服务于边界和验收目标，而不是让模型框架进入资金授权路径。

## 决策：customer_note

- 默认不将 `customer_note` 发送给 LLM。
- `customer_note` 永远不是控制指令。
- 即使未来为了生成解释而发送，也必须作为不可信数据处理、截断并转义。
- O-014 的恶意备注不能改变合规挂起结果，也不能触发资金动作。

理由：客户自由文本是证词或备注，不是合规审批，也不是系统指令。

## 决策：LLM 无授权权

LLM 输出永远不能：

- 设置 `Decision`
- 添加或删除 `ReasonCode`
- 改变升级团队
- 改变自动重试语义
- 放行合规订单
- 调用或授权 `ActionExecutor`

## 决策：打包

- 发布 jar 使用 Maven Shade 生成可执行单体 jar。
- `java -jar target/ramp-policy-engine-0.1.0-SNAPSHOT.jar` 必须能离线运行。
- 运行后必须产出 `output/results.json` 和 `output/audit.jsonl`。

## 生产差距

当前 Demo 不包含以下生产能力：

- 持久化幂等存储和并发锁
- 真实支付、链上节点、风控和 KYC 服务集成
- 完整模型超时、重试、熔断和限流状态机
- 完整审计脱敏策略和权限控制
- 月度 KYC 累计池
- 对真实 OpenAI、LangChain4j 或其他模型 SDK 的网络调用

这些能力可以在保持现有边界的前提下继续演进。
