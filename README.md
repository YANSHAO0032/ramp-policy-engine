# ramp-policy-engine

出入金异常订单分诊的确定性策略引擎 Demo。

核心定位：

```text
Agent investigates. Policy decides. Gates authorize. Executor acts. LLM only explains.
```

也就是说，本项目的 Agent 负责按订单类型收集规则所需事实并编排执行；资金能否放行由 `PolicyEngine`、幂等门和 `ActionExecutor` 共同决定。LLM 只生成 `explanation` 文本，不能修改 `Decision`、`ReasonCode`、升级团队、重试语义或资金动作。

## 工程架构图

```text
┌──────────────────────────┐
│        Demo Orders       │  orders.jsonl
└─────────────┬────────────┘
              │
              v
┌──────────────────────────┐
│      JsonlOrderReader    │
└─────────────┬────────────┘
              │
              v
┌──────────────────────────────────────────────────────────────┐
│                         PolicyBatchRunner                    │
│  ┌──────────────────┐   ┌─────────────────────────────────┐  │
│  │    DemoFacts     │   │         ExplanationProvider     │  │
│  │ customers/assets │   │  stub / recorded / openai       │  │
│  │ address/rates    │   │  only explains, never decides   │  │
│  └─────────┬────────┘   └─────────────────────────────────┘  │
│            │                         ▲                       │
│            v                         │                       │
│    ┌──────────────────┐              │                       │
│    │   PolicyEngine   │              │                       │
│    │ rules + evidence │              │                       │
│    └─────────┬────────┘              │                       │
│              │                       │                       │
│              v                       │                       │
│    ┌──────────────────┐              │                       │
│    │  IdempotencyGate │              │                       │
│    └─────────┬────────┘              │                       │
│              │                       │                       │
│              v                       │                       │
│    ┌──────────────────┐              │                       │
│    │ ActionExecutor   │              │                       │
│    └─────────┬────────┘              │                       │
│              │                       │                       │
│              v                       │                       │
│   results.json / audit.jsonl  ────────┘                       │
└──────────────────────────────────────────────────────────────┘
```

读图方式：

- `PolicyEngine` 只输出确定性决策。
- 幂等门决定能不能重复处理。
- `ActionExecutor` 只在 `COMPLETE` 且幂等通过时执行动作。
- `ExplanationProvider` 只补充解释，不参与授权。

## 状态机转移图

```text
                 ┌──────────────┐
                 │   INPUT      │
                 └──────┬───────┘
                        │
                        v
                 ┌──────────────┐
                 │   EVALUATE   │
                 └──────┬───────┘
                        │
      ┌─────────────────┼─────────────────┬─────────────────┬─────────────────┐
      │                 │                 │                 │                 │
      v                 v                 v                 v                 v
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  COMPLETE    │  │ TEMP_HOLD    │  │ REQUOTE      │  │ OPS_REVIEW   │  │  COMPLIANCE  │
│              │  │              │  │              │  │              │  │    HOLD      │
└──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘
       │                 │                 │                 │                 │
       │                 │                 │                 │                 │
       v                 v                 v                 v                 v
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ Idempotency  │  │ No action    │  │ No action    │  │ No action    │  │ No action    │
│   gates      │  │ + explain    │  │ + explain    │  │ + explain    │  │ + explain    │
└──────┬───────┘  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘
       │
       v
┌──────────────┐
│  ACTION      │
│ EXECUTED?    │
└──────┬───────┘
       │
   yes  v  no
  ┌───────────┐
  │ payout /  │
  │ release   │
  └───────────┘
```

转移规则简表：

- 命中制裁或冻结级风险，直接进入 `FREEZE`。
- 命中合规阻断，进入 `COMPLIANCE_HOLD`。
- 不支持、过期、缺事实或资金异常，分别进入对应人工/运维状态。
- 只有 `COMPLETE` 才继续走幂等门和执行门。

## LLM 边界图

```text
LLM_PROVIDER
   │
   v
LlmProperties
   │
   v
ExplanationProviderFactory
   ├── STUB      -> StubExplanationProvider
   ├── RECORDED  -> RecordedExplanationProvider -> fallback stub
   └── OPENAI    -> OpenAiExplanationProvider -> SafeExplanationProvider -> fallback stub
                                                │
                                                v
                                     explanation text only
                                                │
                                                v
                                      results.json / audit.jsonl
```

这条链路的边界很硬：

- 解释器只能产出文本。
- 文本不能改 `Decision`。
- 文本不能改 `ReasonCode`。
- 文本不能触发资金动作。

## 项目结构

```text
src/main/java/com/ramppolicy/engine/
├── App.java                         # Demo 入口
├── domain/                          # 订单、决策、原因码等不可变领域对象
├── facts/                           # Demo 权威事实加载与 USD 估值
├── idempotency/                     # 订单级和交易级幂等门
├── infrastructure/llm/              # 可替换解释器边界，默认离线
├── io/                              # JSONL 订单读取
├── plan/                            # 按订单类型解析规则计划
├── policy/                          # 确定性策略引擎与结果聚合
└── runtime/                         # 批处理编排、审计、动作执行门

src/main/resources/demo-data/
├── policy.md                        # Demo 策略的唯一事实来源
├── customers.json                   # 客户/KYC/银行名事实
├── assets.json                      # 资产网络配置与确认数
├── address_risk.json                # 地址风险事实
├── reference_rates.json             # 参考汇率
└── orders.jsonl                     # 14 条样例订单
```

## 构建与测试

需要 JDK 21 和 Maven。

```bash
mvn clean test
mvn package
```

Windows PowerShell:

```powershell
mvn clean test
mvn package
```

## 运行

```bash
java -jar target/ramp-policy-engine-0.1.0-SNAPSHOT.jar
```

Windows PowerShell:

```powershell
java -jar target\ramp-policy-engine-0.1.0-SNAPSHOT.jar
```

运行后生成：

```text
output/results.json
output/audit.jsonl
```

## 输出说明

`output/results.json` 是每单最终结果，主要字段包括：

| 字段 | 含义 |
| --- | --- |
| `orderId` | 订单编号 |
| `decision` | 确定性策略结果，例如 `COMPLETE`、`COMPLIANCE_HOLD`、`FREEZE` |
| `reasonCodes` | 机器可读原因码集合 |
| `escalationTargets` | 需要升级的人工团队 |
| `retryability` | 自动重试语义 |
| `actionExecuted` | 是否真的执行资金动作 |
| `actionType` | 已执行的动作类型，未执行时为空 |
| `evidence` | 规则命中的证据文本 |
| `explanation` | 非权威解释文本 |
| `explanationProvider` | 使用的解释器类型 |
| `explanationFallbackUsed` | 是否使用了离线备用解释 |

`output/audit.jsonl` 是逐行审计轨迹。审计里的 `decision`、`reasons` 和资金动作字段来自确定性策略和执行门，不来自 LLM。

## LLM Provider

默认解释器是 `stub`：

- 不访问公网
- 不需要付费 API Key
- 输出确定、可重复
- 只影响 `explanation`

```bash
# 默认：stub
java -jar target/ramp-policy-engine-0.1.0-SNAPSHOT.jar

# 显式指定 stub
LLM_PROVIDER=stub java -jar target/ramp-policy-engine-0.1.0-SNAPSHOT.jar

# 本地录制回放，未命中时回退到 stub
LLM_PROVIDER=recorded java -jar target/ramp-policy-engine-0.1.0-SNAPSHOT.jar
```

PowerShell:

```powershell
$env:LLM_PROVIDER="stub"
java -jar target\ramp-policy-engine-0.1.0-SNAPSHOT.jar

$env:LLM_PROVIDER="recorded"
java -jar target\ramp-policy-engine-0.1.0-SNAPSHOT.jar
```

OpenAI 解释器只在显式配置时启用：

```bash
LLM_PROVIDER=openai \
OPENAI_API_KEY=your-key \
OPENAI_MODEL=gpt-5-mini \
java -jar target/ramp-policy-engine-0.1.0-SNAPSHOT.jar
```

PowerShell:

```powershell
$env:LLM_PROVIDER="openai"
$env:OPENAI_API_KEY="your-key"
$env:OPENAI_MODEL="gpt-5-mini"
java -jar target\ramp-policy-engine-0.1.0-SNAPSHOT.jar
```

当前 Demo 的 `OpenAiExplanationProvider` 是隔离边界：只有 `LLM_PROVIDER=openai` 且提供 `OPENAI_API_KEY` 时才会构造；离线 Demo 不包含真实网络调用，运行时失败会回退到 `stub`，且不会改变任何资金决策。

### 配置项

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `LLM_PROVIDER` | `stub` | 可选 `stub`、`recorded`、`openai` |
| `OPENAI_API_KEY` | 无 | 仅 `LLM_PROVIDER=openai` 时要求 |
| `OPENAI_MODEL` | `gpt-5-mini` | 仅 OpenAI 模式使用 |
| `LLM_RECORDED_FILE` | `classpath:recorded-explanations.json` | 录制回放来源，当前 Demo 使用内置录制映射 |
| `LLM_TIMEOUT_MS` | `3000` | 解释器超时边界配置 |
| `LLM_MAX_INPUT_CHARS` | `8000` | 解释输入上限配置 |
| `LLM_MAX_OUTPUT_CHARS` | `2000` | 解释输出上限配置 |

## 为什么没有使用 LangChain4j

本项目没有引入 LangChain4j 是一个有意的架构取舍，而不是遗漏：

- 题目要求的是“模型可替换、无付费 Key 可离线运行、LLM 放在自有接口后面”，当前 `ExplanationProvider` 已满足这个边界。
- Demo 是 Maven + Java 21 的轻量确定性策略引擎，引入 LangChain4j 会增加依赖、配置和测试面，但不会提升资金主链的正确性。
- LLM 在这里没有工具规划、记忆、链式调用或资金授权职责，只是把 `DeterministicDecision` 转成解释文本。
- 如果未来接入真实模型，可以在 `infrastructure/llm/OpenAiExplanationProvider` 内部选择 LangChain4j、OpenAI SDK 或其他 HTTP 客户端；业务层仍只依赖项目自有接口。

## Golden 样例

内置 `orders.jsonl` 覆盖 14 个关键场景：

| 订单 | 期望结果 | 核心原因 |
| --- | --- | --- |
| O-001 | `COMPLETE` | 干净的法币换加密资产订单 |
| O-002 | `COMPLETE` | 干净的加密资产换法币订单 |
| O-003 | `FREEZE` | 制裁地址 |
| O-004 | `COMPLIANCE_HOLD` | mixer 地址 |
| O-005 | `OPS_REVIEW` | 到账金额不匹配，出款超过已确认价值 |
| O-006 | `REQUOTE` | 报价过期且超过滑点容忍 |
| O-007 | `OPS_REVIEW` | 到账网络与订单网络不一致 |
| O-008 | `OPS_REVIEW` | 不支持的资产或网络 |
| O-009 | `TEMPORARY_HOLD` | 链上确认数不足 |
| O-010 | `COMPLIANCE_HOLD` | KYC 单笔限额超出 |
| O-011 | `COMPLIANCE_HOLD` | Travel Rule 受益人信息缺失 |
| O-012 | `REJECT` | 银行账户户名不匹配 |
| O-013 | `OPS_REVIEW` | 重复交易哈希 |
| O-014 | `COMPLIANCE_HOLD` | mixer 地址；客户备注不作为放行依据 |

## Evaluation & Reliability

The original 14 orders are regression baselines, not the full evaluation set.

The evaluation suite also covers policy boundary cases, multi-rule conflicts, seeded 10,000-order generation, LLM nondeterminism, prompt injection, tool failure, and concurrent idempotency.

### LLM nondeterminism

The LLM is not part of the financial authorization path.

Decision, ReasonCodes, EscalationTargets, Retryability, idempotency results, and financial action authorization are finalized before explanation generation.

Random and malicious explanation providers intentionally produce different or adversarial text for identical orders.

Required invariants:

- Decision drift = 0
- ReasonCode drift = 0
- Action drift = 0
- Unauthorized payout = 0

> The model may be nondeterministic; money movement must not be.

### Latest offline evaluation

Evaluation commit: `37dd5c7c23300c923d3fe240cd2ecf25e781c1c4`

| Metric | Result |
|---|---:|
| Random seed | 20260819 |
| Generated orders | 10,000 |
| Processed orders | 10,000 |
| Golden regression | 14 / 14 |
| Boundary cases | 31 / 31 |
| Conflict cases | 6 / 6 |
| Safety invariant checks | 261,326 |
| Safety invariant violations | 0 |
| Unauthorized payouts | 0 |
| Duplicate payouts | 0 |
| Prompt injection cases | 140 |
| Prompt injection bypasses | 0 |
| Tool failure cases | 1,000 |
| Unsafe fail-open cases | 0 |
| LLM variation calls | 2,800 |
| Decision drift | 0 |
| ReasonCode drift | 0 |
| Action drift | 0 |
| Paid LLM calls | 0 |
| Total duration ms | 460 |
| Average latency ms | 0.046000 |
| Final result | PASS |

### Reproduce

```bash
EVAL_RANDOM_SEED=20260819 \
EVAL_ORDER_COUNT=10000 \
mvn test -Peval
```

Windows PowerShell:

```powershell
$env:EVAL_RANDOM_SEED="20260819"
$env:EVAL_ORDER_COUNT="10000"
mvn test -Peval
```

Reports:

```text
target/evaluation/evaluation-report.json
target/evaluation/evaluation-report.md
```

## 策略假设

- `src/main/resources/demo-data/policy.md` 优先于实现细节。
- 事实源固定为 `src/main/resources/demo-data/`。
- 评估时钟固定为 `2026-07-28T12:00:00Z`。
- 所有金额、汇率、滑点计算使用 `BigDecimal`。
- 未知事实按 fail-closed 处理，不自行推断。
- KYC 仅按 Demo 数据做单笔判断，不实现月度累计池。
- `customer_note` 永远不是控制指令。

## 新增规则指引

新增规则时建议按这个顺序处理：

1. 在 `RuleId` 和 `StaticRulePlanResolver` 中声明适用订单类型。
2. 在 `FactRequirement` 中补充规则需要的事实类别。
3. 在 `PolicyEngine` 中实现确定性规则函数。
4. 在 `ReasonCode` 和 `EscalationTarget` 中补充结构化输出。
5. 为边界场景、Golden 样例和幂等/安全不变量补测试。
6. 更新 `DECISIONS.md`，说明策略留白如何被解释。

新增真实 LLM 能力时，只能放在 `infrastructure/llm` 边界内，不能让 `policy`、`domain`、`idempotency` 或 `runtime/ActionExecutor` 依赖模型 SDK。

## 排查

常用命令：

```bash
mvn clean test
mvn package
java -jar target/ramp-policy-engine-0.1.0-SNAPSHOT.jar
```

PowerShell 清空 LLM 环境后做离线回归：

```powershell
Remove-Item Env:LLM_PROVIDER -ErrorAction SilentlyContinue
Remove-Item Env:OPENAI_API_KEY -ErrorAction SilentlyContinue
Remove-Item Env:OPENAI_MODEL -ErrorAction SilentlyContinue
mvn clean test
```

确认 LLM 没有进入资金主链：

```bash
rg -n "Explanation|Llm|OpenAi" src/main/java/com/ramppolicy/engine/policy src/main/java/com/ramppolicy/engine/domain src/main/java/com/ramppolicy/engine/idempotency src/main/java/com/ramppolicy/engine/runtime/ActionExecutor.java
```

该命令应没有业务授权路径相关输出。
