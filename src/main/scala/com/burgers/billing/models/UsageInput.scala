package com.burgers.billing.models

import cats.effect.kernel.Concurrent
import io.circe.generic.semiauto.deriveCodec
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

final case class UsageInput(
  date: InputDate,
  units: InputUsageUnits,
  amount: BigDecimal
)

object UsageInput {
  implicit val codec = deriveCodec[UsageInput]
  implicit def entityDecoder[F[_]: Concurrent]: EntityDecoder[F, UsageInput] = jsonOf
}
