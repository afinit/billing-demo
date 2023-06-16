package com.burgers.billing.models

import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

case class Invoice(
  id: Int,
  total: BigDecimal,
  invoiceItems: Vector[InvoiceItem]
)

object Invoice {
  implicit val encoder = deriveEncoder[Invoice]
  implicit def entityEncoder[F[_]]: EntityEncoder[F, Invoice] = jsonEncoderOf
}
