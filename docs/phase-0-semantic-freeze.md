# Phase 0: Semantic Freeze

## Source files

- `D:/java_workspace/Java(MSB)笔试题/files/policy.md`
- `D:/java_workspace/Java(MSB)笔试题/files/customers.json`
- `D:/java_workspace/Java(MSB)笔试题/files/assets.json`
- `D:/java_workspace/Java(MSB)笔试题/files/address_risk.json`
- `D:/java_workspace/Java(MSB)笔试题/files/reference_rates.json`
- `D:/java_workspace/Java(MSB)笔试题/files/orders.jsonl`

## Order field map

| Type | Required fields from `orders.jsonl` |
| --- | --- |
| `on_ramp` | `order_id`, `type`, `customer_id`, `asset`, `network`, `fiat_amount_usd`, `quoted_crypto_amount`, `quote_expires_at`, `fiat_status`, `destination_address` |
| `off_ramp` | `order_id`, `type`, `customer_id`, `asset`, `network`, `quoted_crypto_amount`, `quote_expires_at`, `deposit.tx_hash`, `deposit.from_address`, `deposit.confirmations`, `deposit.observed_amount`, `payout.bank_account_name`, `payout.currency`, `payout.amount` |
| `withdrawal` | `order_id`, `type`, `customer_id`, `asset`, `network`, `amount`, `destination_address` |

## Required-field matrix

| Requirement | on_ramp | off_ramp | withdrawal |
| --- | --- | --- | --- |
| Customer | yes | yes | yes |
| Asset / network | yes | yes | yes |
| Address risk target | destination | source | destination |
| Fiat receipt | yes | no | no |
| Deposit | no | yes | no |
| Payout | no | yes | no |
| Wallet funds / reservation evidence | no | no | yes |

## Rule applicability matrix

| Rule | on_ramp | off_ramp | withdrawal |
| --- | --- | --- | --- |
| CustomerStatusRule | yes | yes | yes |
| AssetSupportRule | yes | yes | yes |
| AddressRiskRule | yes | yes | yes |
| KycLimitRule | yes | yes | yes |
| MinimumAmountRule | yes | yes | yes |
| FiatReceiptRule | yes | no | no |
| OnRampConservationRule | yes | no | no |
| ConfirmationRule | no | yes | no |
| AmountMatchRule | no | yes | no |
| PayoutConservationRule | no | yes | no |
| NetworkMatchRule | no | yes | no |
| BankOwnershipRule | no | yes | no |
| QuoteExpiryRule | yes | yes | no |
| TravelRule | conditional | conditional | conditional |
| WithdrawalFundsRule | no | no | yes |
| VaspUnknownWarningRule | advisory | advisory | advisory |

## Golden baseline

| Order | Expected | Key reason(s) |
| --- | --- | --- |
| O-001 | COMPLETE | clean on-ramp |
| O-002 | COMPLETE | clean off-ramp |
| O-003 | FREEZE | sanctioned address |
| O-004 | COMPLIANCE_HOLD | mixer address |
| O-005 | OPS_REVIEW | amount mismatch and payout exceeds confirmed value |
| O-006 | REQUOTE | quote expired beyond slippage tolerance |
| O-007 | OPS_REVIEW | network mismatch |
| O-008 | OPS_REVIEW | unsupported asset |
| O-009 | TEMPORARY_HOLD | insufficient confirmations |
| O-010 | COMPLIANCE_HOLD | KYC limit exceeded |
| O-011 | COMPLIANCE_HOLD | travel rule info missing |
| O-012 | REJECT | bank name mismatch |
| O-013 | OPS_REVIEW | duplicate transaction |
| O-014 | COMPLIANCE_HOLD | mixer address; note is ignored |

## Policy leave blanks

- Minimum amount behavior is not specified in `policy.md`.
- Withdrawal fund evidence is not specified in `policy.md`.
- VASP-unknown handling is not specified in `policy.md`.
- Month-to-date KYC pooling is not specified in `policy.md`.
- Actual settled fiat amount is not available as a separate field in the demo data.

## Policy version

- `MSB-V4`
