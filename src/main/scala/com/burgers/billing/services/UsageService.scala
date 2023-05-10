package com.burgers.billing.services

import cats.MonadThrow
import cats.implicits._
import com.burgers.billing.models.{UsageInput, UsageUnits}

import java.time.LocalDate

trait UsageService[F[_]] {

  def create(usageInput: UsageInput): F[Int]

//  def readByUnits(units: Units): F[Vector[Usage]]

}

object UsageService {
  def build[F[_]: MonadThrow]: UsageService[F] = new UsageServiceImpl[F]

  private[services] val badAmountError = new Exception("amount must be greater than or equal to 0")

  private[services] def validateDate(usageInput: UsageInput): Either[Exception, LocalDate] =
    try {
      LocalDate.parse(usageInput.date).asRight[Exception]
    } catch {
      case e: Exception =>
        e.asLeft[LocalDate]
    }

  private[services] def validateUsageUnits(usageInput: UsageInput): Either[Exception, UsageUnits] =
    UsageUnits.fromString(usageInput.units)

  private[services] def validateAmount(input: UsageInput): Either[Exception, BigDecimal] =
    if (input.amount < 0) badAmountError.asLeft[BigDecimal]
    else input.amount.asRight[Exception]
}

class UsageServiceImpl[F[_]: MonadThrow] extends UsageService[F] {
  import UsageService._

  override def create(usageInput: UsageInput): F[Int] = {
    for {
      _ <- validateDate(usageInput).liftTo[F]
      _ <- validateUsageUnits(usageInput).liftTo[F]
      _ <- validateAmount(usageInput).liftTo[F]
      resp <- 1.pure[F]
    } yield resp
  }
}
