package com.burgers.billing.repos

import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator

import java.time.LocalDate

class InvoiceRepositorySqlite extends InvoiceRepository[ConnectionIO] {

  import DbImplicits._

  override def createInvoice(date: LocalDate): ConnectionIO[Int] = {
    val q =
      sql"""
           |INSERT INTO invoice(invoiceDateEpoch)
           |VALUES (${date.toEpochDay})
           |""".stripMargin
    q.update.withUniqueGeneratedKeys[Int]("rowid")
  }

  override def getInvoiceDate(id: Int): ConnectionIO[Option[LocalDate]] = {
    val q =
      sql"""
        |SELECT invoiceDateEpoch
        |FROM invoice
        |WHERE rowid = $id
        |""".stripMargin

    q.query[LocalDate].option
  }

}
