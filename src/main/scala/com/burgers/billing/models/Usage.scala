package com.burgers.billing.models

import java.time.LocalDate

final case class Usage(
  id: String,
  date: LocalDate,
  units: UsageUnits,
  amount: BigDecimal
)
