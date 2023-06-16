package com.burgers.billing.repos

import cats.data.NonEmptyList
import com.burgers.billing.models.{Usage, UsageUnits}
import doobie._
import doobie.implicits._
import doobie.util.fragments.{whereAnd, whereAndOpt}
import cats.implicits._

import java.time.LocalDate

class UsageRepositorySqlite extends UsageRepository[ConnectionIO] {

  import UsageRepositorySqlite._

  override def getNewInvoiceId: ConnectionIO[Int] = ???

  override def create(date: LocalDate, usageUnits: UsageUnits, amount: BigDecimal): ConnectionIO[Usage] = for {
    id <-
      sql"""
           |INSERT INTO usage(dateEpoch, usageUnits, amount)
           |VALUES (${date.toEpochDay}, ${usageUnits.toString}, ${amount})
           |""".stripMargin.update.run
  } yield Usage(id, date, usageUnits, amount, None)

  override def get(
    idFilter: Option[Int],
    startDateFilter: Option[LocalDate],
    endDateFilter: Option[LocalDate],
    usageUnitsFilter: Option[UsageUnits],
    invoicedFilter: Option[Boolean]
  ): ConnectionIO[Vector[Usage]] = {
    val idClause = idFilter.map(id => fr"ID = ${id}")
    val startDateClause = startDateFilter.map(sd => fr"dateEpoch >= ${sd.toEpochDay}")
    val endDateClause = endDateFilter.map(ed => fr"dateEpoch <= ${ed.toEpochDay}")
    val usageUnitsClause = usageUnitsFilter.map(uu => fr"usageUnits = ${uu.toString}")
    val invoicedClause = invoicedFilter.map(iFlag => if (iFlag) fr"invoiceId IS NOT NULL" else fr"invoiceId IS NULL")

    val q = fr"SELECT * FROM usage" ++
      whereAndOpt(idClause, startDateClause, endDateClause, usageUnitsClause, invoicedClause)
    q.query[Usage].to[Vector]
  }

  override def getByInvoiceId(invoiceId: Int): ConnectionIO[Vector[Usage]] = {
    val idClause = fr"invoiceId = ${invoiceId}"
    val q = fr"SELECT * FROM usage" ++
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
            |""".stripMargin ++ Fragments.in(fr"id", usageIdsNE)
      q.update.run.void
    }
  }

}

object UsageRepositorySqlite {
  private def addUsageTable =
    sql"""
         |CREATE TABLE IF NOT EXISTS usage (
         |  id INTEGER PRIMARY KEY,
         |  dateEpoch INT NOT NULL,
         |  usageUnits TEXT NOT NULL,
         |  amount NUMERIC NOT NULL,
         |  invoiceId INT
         |)
         |""".stripMargin.update.run

  def build: ConnectionIO[UsageRepository[ConnectionIO]] = for {
    _ <- addUsageTable
  } yield new UsageRepositorySqlite

  implicit val localDateGet: Get[LocalDate] = Get[Int].tmap(l => LocalDate.ofEpochDay(l.toLong))
  implicit val usageUnitsGet: Get[UsageUnits] = Get[String].tmap(UsageUnits.fromString(_).fold(e => throw e, identity))
  implicit val bigDecimal: Get[BigDecimal] = Get[Double].tmap(BigDecimal(_))
}
