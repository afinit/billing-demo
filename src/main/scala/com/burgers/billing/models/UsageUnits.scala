package com.burgers.billing.models

import cats.implicits.catsSyntaxEitherId

sealed trait UsageUnits

object UsageUnits {
  case object StorageBytes extends UsageUnits
  case object Cpu extends UsageUnits
  case object BandwidthBytes extends UsageUnits

  val badUnitsError: Exception = new Exception("UsageUnits must be input as one of: storagebytes, cpu, bandwidthbytes")

  def fromString(input: String): Either[Exception, UsageUnits] = input.toLowerCase match {
    case "storagebytes" => StorageBytes.asRight[Exception]
    case "cpu" => Cpu.asRight[Exception]
    case "bandwidthbytes" => BandwidthBytes.asRight[Exception]
    case _ => badUnitsError.asLeft[UsageUnits]
  }
}
