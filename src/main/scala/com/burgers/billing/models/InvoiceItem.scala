package com.burgers.billing.models

import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

import java.time.LocalDate

final case class InvoiceItem(
  date: LocalDate,
  units: UsageUnits,
  amount: BigDecimal,
  totalCost: BigDecimal,
)

object InvoiceItem {
  implicit val encoder = deriveEncoder[InvoiceItem]
  implicit def entityEncoder[F[_]]: EntityEncoder[F, Vector[InvoiceItem]] = jsonEncoderOf
}
