package com.burgers.billing.repos

import doobie.ConnectionIO

import java.time.LocalDate

trait InvoiceRepository[F[_]] {

  def createInvoice(
    date: LocalDate
  ): F[Int]

  def getInvoiceDate(
    id: Int
  ): F[Option[LocalDate]]

}

object InvoiceRepository {

  def buildSqlite: InvoiceRepository[ConnectionIO] = new InvoiceRepositorySqlite

}
