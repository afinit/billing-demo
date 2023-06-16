package com.burgers.billing.models

import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

import java.time.LocalDate

final case class Usage(
  id: Int,
  date: LocalDate,
  units: UsageUnits,
  amount: BigDecimal,
  invoiceId: Option[Int]
)

object Usage {
  implicit val encoder = deriveEncoder[Usage]
  implicit def entityEncoder[F[_]]: EntityEncoder[F, Vector[Usage]] = jsonEncoderOf
}
