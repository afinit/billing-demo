package com.burgers.billing.repos

import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator

object SqliteDbSetup {

  private def createInvoiceTable =
    sql"""
         |CREATE TABLE IF NOT EXISTS invoice (
         |  invoiceDateEpoch INT NOT NULL
         |)
         |""".stripMargin.update.run

  private def createUsageTable =
    sql"""
         |CREATE TABLE IF NOT EXISTS usage (
         |  dateEpoch INT NOT NULL,
         |  usageUnits TEXT NOT NULL,
         |  amount NUMERIC NOT NULL,
         |  invoiceId INT,
         |  FOREIGN KEY(invoiceId) REFERENCES invoice(rowid)
         |)
         |""".stripMargin.update.run

  // establishes ease of use for a toy application. the goal is to make sure this is easy to spin up with a fresh db
  def createTables: ConnectionIO[Unit] = for {
    _ <- createInvoiceTable
    _ <- createUsageTable
  } yield ()

}
