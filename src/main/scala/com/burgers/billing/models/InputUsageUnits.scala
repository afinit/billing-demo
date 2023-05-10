package com.burgers.billing.models

import cats.effect.kernel.Concurrent
import io.circe.generic.semiauto.deriveCodec
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

final case class InputUsageUnits(
  units: String
) extends AnyVal

object InputUsageUnits {
  implicit val codec = deriveCodec[InputUsageUnits]
  implicit def entityDecoder[F[_] : Concurrent]: EntityDecoder[F, InputUsageUnits] = jsonOf
}
