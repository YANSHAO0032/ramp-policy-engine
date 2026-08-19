# DECISIONS

## Demo facts

- Demo 事实直接 vendored 到仓库。
- `customers.json`、`assets.json`、`address_risk.json`、`reference_rates.json` 是权威输入。
- `orders.jsonl` 是批处理样例输入。

## Time

- 全链路使用 UTC。
- Demo 评估时钟固定为 `2026-07-28T12:00:00Z`。

## Money

- 金额、汇率、滑点统一使用 `BigDecimal`。
- 不使用 `double` / `float` 参与金额判定。

## Idempotency

- order-level gate 先于资金动作。
- off-ramp COMPLETE candidate 还要过 transaction-level gate。
- duplicate order / duplicate transaction 都只产生结构化拒绝，不产生第二次资金动作。

## Demo KYC

- 只检查单笔，不做月度累计。
- KYC 统一以 USD 口径比较。

## Travel Rule

- 只有明确 `is_vasp=true` 且 crypto transfer USD >= 1000 时才强制触发。
- VASP unknown 仅记 warning，不自行推断。

## Packaging

- 发布 jar 使用 shade 生成可执行单体 jar。
- `java -jar` 必须能离线运行并产出 `output/results.json` 与 `output/audit.jsonl`。
