package com.burgers.billing.services

import cats.MonadThrow
import cats.implicits._
import com.burgers.billing.models._
import com.burgers.billing.repos.UsageRepository
import com.typesafe.scalalogging.StrictLogging

import java.time.LocalDate

trait UsageService[F[_]] {

  def create(usageInput: UsageInput): F[String]

  def get(usageFilterInput: UsageFilterInput): F[Vector[Usage]]

}

object UsageService {
  def build[F[_] : MonadThrow](usageRepository: UsageRepository[F]): UsageService[F] =
    new UsageServiceImpl[F](usageRepository)

  private[services] val badAmountError = new Exception("amount must be greater than or equal to 0")

  private[services] def validateDate(inputDate: InputDate): Either[Exception, LocalDate] =
    try {
      LocalDate.parse(inputDate.dateString).asRight[Exception]
    } catch {
      case e: Exception =>
        e.asLeft[LocalDate]
    }

  private[services] def validateUsageUnits(inputUsageUnits: InputUsageUnits): Either[Exception, UsageUnits] =
    UsageUnits.fromString(inputUsageUnits.units)

  private[services] def validateAmount(input: UsageInput): Either[Exception, BigDecimal] =
    if (input.amount < 0) badAmountError.asLeft[BigDecimal]
    else input.amount.asRight[Exception]
}

class UsageServiceImpl[F[_] : MonadThrow](
  usageRepo: UsageRepository[F]
) extends UsageService[F] with StrictLogging {

  import UsageService._

  override def create(usageInput: UsageInput): F[String] = {
    for {
      date <- validateDate(usageInput.date).liftTo[F]
      usageUnits <- validateUsageUnits(usageInput.units).liftTo[F]
      amount <- validateAmount(usageInput).liftTo[F]
      resp <- usageRepo.create(date, usageUnits, amount)
    } yield resp.id
  }

  override def get(usageFilterInput: UsageFilterInput): F[Vector[Usage]] = {
    logger.info(s"get input: ${usageFilterInput}")
    for {
      startDate <- usageFilterInput.startDate.map(validateDate).sequence.liftTo[F]
      endDate <- usageFilterInput.endDate.map(validateDate).sequence.liftTo[F]
      usageUnits <- usageFilterInput.usageUnits.map(validateUsageUnits).sequence.liftTo[F]
      resp <- usageRepo.get(
        idFilter = usageFilterInput.id,
        startDateFilter = startDate,
        endDateFilter = endDate,
        usageUnitsFilter = usageUnits
      )
    } yield resp
  }
}
