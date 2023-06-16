package com.burgers.billing.repos

import com.burgers.billing.models.UsageUnits
import doobie.Get

import java.time.LocalDate

object DbImplicits {

  implicit val localDateGet: Get[LocalDate] = Get[Int].tmap(l => LocalDate.ofEpochDay(l.toLong))
  implicit val usageUnitsGet: Get[UsageUnits] = Get[String].tmap(UsageUnits.fromString(_).fold(e => throw e, identity))
  implicit val bigDecimal: Get[BigDecimal] = Get[Double].tmap(BigDecimal(_))
}
