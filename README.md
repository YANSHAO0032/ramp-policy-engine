# ramp-policy-engine

出入金异常订单分诊的确定性策略引擎 Demo。

## Build

```bash
mvn test
mvn package
```

## Run

```bash
java -jar target/ramp-policy-engine-0.1.0-SNAPSHOT.jar
```

运行后会生成：

```text
output/results.json
output/audit.jsonl
```

## Demo assumptions

- 事实源固定为 `src/main/resources/demo-data/`
- 评估时钟固定为 `2026-07-28T12:00:00Z`
- `policy.md` 优先于实现细节
- 任何不确定事实都按 fail-closed 处理

## Debug

- 单测：`mvn test`
- 打包：`mvn package`
- 产物：`target/ramp-policy-engine-0.1.0-SNAPSHOT.jar`
