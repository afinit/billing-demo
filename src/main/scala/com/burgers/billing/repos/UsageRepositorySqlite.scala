package com.burgers.billing.repos

import cats.data.NonEmptyList
import com.burgers.billing.models.{Usage, UsageUnits}
import doobie._
import doobie.implicits._
import doobie.util.fragments.{whereAnd, whereAndOpt}
import cats.implicits._

import java.time.LocalDate

class UsageRepositorySqlite extends UsageRepository[ConnectionIO] {

  import DbImplicits._

  private def selectUsageColumns = fr"SELECT rowid, dateEpoch, usageUnits, amount, invoiceId FROM usage"

  override def create(date: LocalDate, usageUnits: UsageUnits, amount: BigDecimal): ConnectionIO[Usage] = for {
    id <-
      sql"""
           |INSERT INTO usage(dateEpoch, usageUnits, amount)
           |VALUES (${date.toEpochDay}, ${usageUnits.toString}, ${amount})
           |""".stripMargin.update.withUniqueGeneratedKeys[Int]("rowid")
  } yield Usage(id, date, usageUnits, amount, None)

  override def get(
    idFilter: Option[Int],
    startDateFilter: Option[LocalDate],
    endDateFilter: Option[LocalDate],
    usageUnitsFilter: Option[UsageUnits],
    invoicedFilter: Option[Boolean]
  ): ConnectionIO[Vector[Usage]] = {
    val idClause = idFilter.map(id => fr"ROWID = ${id}")
    val startDateClause = startDateFilter.map(sd => fr"dateEpoch >= ${sd.toEpochDay}")
    val endDateClause = endDateFilter.map(ed => fr"dateEpoch <= ${ed.toEpochDay}")
    val usageUnitsClause = usageUnitsFilter.map(uu => fr"usageUnits = ${uu.toString}")
    val invoicedClause = invoicedFilter.map(iFlag => if (iFlag) fr"invoiceId IS NOT NULL" else fr"invoiceId IS NULL")

    val q = selectUsageColumns ++
      whereAndOpt(idClause, startDateClause, endDateClause, usageUnitsClause, invoicedClause)
    q.query[Usage].to[Vector]
  }

  override def getByInvoiceId(invoiceId: Int): ConnectionIO[Vector[Usage]] = {
    val idClause = fr"invoiceId = ${invoiceId}"
    val q = selectUsageColumns ++
      whereAnd(idClause)

    q.query[Usage].to[Vector]
  }

  override def updateUsageWithInvoice(usageIds: Vector[Int], invoiceId: Int): ConnectionIO[Unit] = {
    NonEmptyList.fromList(usageIds.toList).fold(().pure[ConnectionIO]) { usageIdsNE =>
      val q =
        fr"""
            |UPDATE usage
            |SET invoiceId = ${invoiceId}
            |WHERE
            |""".stripMargin ++ Fragments.in(fr"rowid", usageIdsNE)
      q.update.run.void
    }
  }

}
