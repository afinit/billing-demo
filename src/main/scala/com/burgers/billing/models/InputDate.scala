package com.burgers.billing.models

import cats.effect.kernel.Concurrent
import io.circe.generic.semiauto.deriveCodec
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

case class InputDate(
  dateString: String
) extends AnyVal

object InputDate {
  implicit val codec = deriveCodec[InputDate]
  implicit def entityDecoder[F[_] : Concurrent]: EntityDecoder[F, InputDate] = jsonOf
}
