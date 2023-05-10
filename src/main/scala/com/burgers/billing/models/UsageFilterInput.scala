package com.burgers.billing.models

import cats.effect.kernel.Concurrent
import io.circe.generic.semiauto.deriveCodec
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

case class UsageFilterInput(
  id: Option[String],
  startDate: Option[InputDate],
  endDate: Option[InputDate],
  usageUnits: Option[InputUsageUnits]
)

object UsageFilterInput {
  implicit val codec = deriveCodec[UsageFilterInput]
  implicit def entityDecoder[F[_] : Concurrent]: EntityDecoder[F, UsageFilterInput] = jsonOf
}
