package com.burgers.billing.models

import cats.Show
import enumeratum._
import org.http4s.EntityEncoder

sealed trait UsageUnits extends EnumEntry

object UsageUnits extends Enum[UsageUnits] with CirceEnum[UsageUnits] {
  val values = findValues
  case object StorageBytes extends UsageUnits
  case object Cpu extends UsageUnits
  case object BandwidthBytes extends UsageUnits

  val badUnitsError: Exception = new Exception(s"UsageUnits must be input as one of: ${values.mkString(",")}")

  def fromString(input: String): Either[Exception, UsageUnits] =
    UsageUnits.withNameInsensitiveOption(input).toRight(badUnitsError)

  def toString(input: UsageUnits): String = input.entryName.toLowerCase

  implicit val show: Show[UsageUnits] = Show.show(toString)
  implicit def entityEncoder[F[_]]: EntityEncoder[F, UsageUnits] = EntityEncoder.showEncoder
}
