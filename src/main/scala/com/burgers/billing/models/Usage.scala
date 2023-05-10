package com.burgers.billing.models

import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

import java.time.LocalDate

final case class Usage(
  id: String,
  date: LocalDate,
  units: UsageUnits,
  amount: BigDecimal
)

object Usage {
  implicit val encoder = deriveEncoder[Usage]
  implicit def entityEncoder[F[_]]: EntityEncoder[F, Vector[Usage]] = jsonEncoderOf
}
