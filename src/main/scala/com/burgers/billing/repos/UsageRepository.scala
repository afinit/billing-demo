package com.burgers.billing.repos

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeId
import com.burgers.billing.models.{Usage, UsageUnits}

import java.time.LocalDate

trait UsageRepository[F[_]] {

  def create(
    date: LocalDate,
    usageUnits: UsageUnits,
    amount: BigDecimal
  ): F[Usage]

  def get(
    idFilter: Option[String],
    startDateFilter: Option[LocalDate],
    endDateFilter: Option[LocalDate],
    usageUnitsFilter: Option[UsageUnits]
  ): F[Vector[Usage]]

}

object UsageRepository {
  def build[F[_] : Applicative]: UsageRepository[F] = new UsageRepositoryImpl[F]

  private[repos] def filterById(idFilter: Option[String], usage: Usage): Boolean =
    idFilter.isEmpty || idFilter.contains(usage.id)

  private[repos] def filterByStartDate(startDateFilter: Option[LocalDate], usage: Usage): Boolean =
    startDateFilter.isEmpty || startDateFilter.exists(!_.isAfter(usage.date))

  private[repos] def filterByEndDate(endDateFilter: Option[LocalDate], usage: Usage): Boolean =
    endDateFilter.isEmpty || endDateFilter.exists(!_.isBefore(usage.date))

  private[repos] def filterByUsageUnits(usageUnitsFilter: Option[UsageUnits], usage: Usage): Boolean =
    usageUnitsFilter.isEmpty || usageUnitsFilter.contains(usage.units)

  private[repos] def filterUsage(
    idFilter: Option[String],
    startDateFilter: Option[LocalDate],
    endDateFilter: Option[LocalDate],
    usageUnitsFilter: Option[UsageUnits],
    usage: Usage
  ): Boolean = filterById(idFilter, usage) &&
    filterByStartDate(startDateFilter, usage) &&
    filterByEndDate(endDateFilter, usage) &&
    filterByUsageUnits(usageUnitsFilter, usage)

}

class UsageRepositoryImpl[F[_] : Applicative] extends UsageRepository[F] {
  import UsageRepository._

  private var currentIdValue: Int = 0

  private def getNewId: Int = {
    currentIdValue += 1
    currentIdValue
  }

  import scala.collection.mutable

  private val usageDataStore = mutable.Map.empty[String, Usage]

  override def create(date: LocalDate, usageUnits: UsageUnits, amount: BigDecimal): F[Usage] = {
    val usageId = getNewId.toString
    val usage = Usage(usageId, date, usageUnits, amount)
    usageDataStore += usage.id -> usage
    usage.pure[F]
  }

  override def get(
    idFilter: Option[String],
    startDateFilter: Option[LocalDate],
    endDateFilter: Option[LocalDate],
    usageUnitsFilter: Option[UsageUnits]
  ): F[Vector[Usage]] = {
    usageDataStore.values
      .filter { filterUsage(idFilter, startDateFilter, endDateFilter, usageUnitsFilter, _) }
      .toVector
      .pure[F]
  }

}