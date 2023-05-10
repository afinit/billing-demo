package com.burgers.billing.models

import cats.effect.kernel.Concurrent
import io.circe.generic.semiauto.deriveCodec
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

case class InvoiceFilterInput(
  startDate: Option[InputDate],
  endDate: Option[InputDate],
  usageUnits: Option[InputUsageUnits]
)

object InvoiceFilterInput {
  implicit val codec = deriveCodec[InvoiceFilterInput]
  implicit def entityDecoder[F[_] : Concurrent]: EntityDecoder[F, InvoiceFilterInput] = jsonOf
}
