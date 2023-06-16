package com.burgers.billing.repos

import com.burgers.billing.models.{Usage, UsageUnits}

import java.time.LocalDate

trait UsageRepository[F[_]] {

  def create(
    date: LocalDate,
    usageUnits: UsageUnits,
    amount: BigDecimal
  ): F[Usage]

  def get(
    idFilter: Option[Int],
    startDateFilter: Option[LocalDate],
    endDateFilter: Option[LocalDate],
    usageUnitsFilter: Option[UsageUnits],
    invoicedFilter: Option[Boolean]
  ): F[Vector[Usage]]

  def getByInvoiceId(invoiceId: Int): F[Vector[Usage]]

  def updateUsageWithInvoice(
    usageIds: Vector[Int],
    invoiceId: Int
  ): F[Unit]

  def createInvoice(
    date: LocalDate
  ): F[Int]

}
