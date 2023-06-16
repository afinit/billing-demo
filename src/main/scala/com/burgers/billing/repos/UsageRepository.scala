package com.burgers.billing.repos

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeId
import com.burgers.billing.models.{Usage, UsageUnits}

import java.time.LocalDate

trait UsageRepository[F[_]] {

  def getNewInvoiceId: F[Int]

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

}

object UsageRepository {
  def build[F[_] : Applicative]: UsageRepository[F] = new UsageRepositoryImpl[F]

  private[repos] def filterById(idFilter: Option[Int], usage: Usage): Boolean =
    idFilter.isEmpty || idFilter.contains(usage.id)

  private[repos] def filterByStartDate(startDateFilter: Option[LocalDate], usage: Usage): Boolean =
    startDateFilter.isEmpty || startDateFilter.exists(!_.isAfter(usage.date))

  private[repos] def filterByEndDate(endDateFilter: Option[LocalDate], usage: Usage): Boolean =
    endDateFilter.isEmpty || endDateFilter.exists(!_.isBefore(usage.date))

  private[repos] def filterByUsageUnits(usageUnitsFilter: Option[UsageUnits], usage: Usage): Boolean =
    usageUnitsFilter.isEmpty || usageUnitsFilter.contains(usage.units)

  /** if invoicedFilter is true, only returns true for a usage that has been invoiced
   *  if invoicedFilter is false, only returns true for a usage that has not been invoiced
   *  else always return true
   *
   * @param invoicedFilter
   * @param usage
   * @return
   */
  private[repos] def filterByIsInvoiced(invoicedFilter: Option[Boolean], usage: Usage): Boolean =
    invoicedFilter.isEmpty || invoicedFilter.contains(usage.invoiceId.nonEmpty)

  private[repos] def filterUsage(
    idFilter: Option[Int],
    startDateFilter: Option[LocalDate],
    endDateFilter: Option[LocalDate],
    usageUnitsFilter: Option[UsageUnits],
    invoicedFilter: Option[Boolean],
    usage: Usage
  ): Boolean = filterById(idFilter, usage) &&
    filterByStartDate(startDateFilter, usage) &&
    filterByEndDate(endDateFilter, usage) &&
    filterByUsageUnits(usageUnitsFilter, usage) &&
    filterByIsInvoiced(invoicedFilter, usage)

}

class UsageRepositoryImpl[F[_] : Applicative] extends UsageRepository[F] {

  import UsageRepository._

  private var currentUsageIdValue: Int = 0

  private def getNewUsageId: Int = {
    currentUsageIdValue += 1
    currentUsageIdValue
  }

  private var currentInvoiceIdValue: Int = 0

  override def getNewInvoiceId: F[Int] = {
    currentInvoiceIdValue += 1
    currentInvoiceIdValue.pure[F]
  }

  import scala.collection.mutable

  private val usageDataStore = mutable.Map.empty[Int, Usage]

  override def create(date: LocalDate, usageUnits: UsageUnits, amount: BigDecimal): F[Usage] = {
    val usageId = getNewUsageId
    val usage = Usage(usageId, date, usageUnits, amount, None)
    usageDataStore += usage.id -> usage
    usage.pure[F]
  }

  override def get(
    idFilter: Option[Int],
    startDateFilter: Option[LocalDate],
    endDateFilter: Option[LocalDate],
    usageUnitsFilter: Option[UsageUnits],
    invoicedFilter: Option[Boolean]
  ): F[Vector[Usage]] = {
    usageDataStore.values
      .filter {
        filterUsage(idFilter, startDateFilter, endDateFilter, usageUnitsFilter, invoicedFilter, _)
      }
      .toVector
      .pure[F]
  }

  override def getByInvoiceId(invoiceId: Int): F[Vector[Usage]] = {
    usageDataStore.values
      .filter(_.invoiceId.contains(invoiceId))
      .toVector
      .pure[F]
  }

  override def updateUsageWithInvoice(usageIds: Vector[Int], invoiceId: Int): F[Unit] = {
    val usages = usageIds.flatMap(usageDataStore.get)
    val updatedUsages = usages.map(_.copy(invoiceId = Some(invoiceId)))
    val updatedUsagesMap = updatedUsages.map(u => u.id -> u)
    usageDataStore ++= updatedUsagesMap
    ().pure[F]
  }

}