package com.burgers.billing.services

import cats.{MonadThrow, ~>}
import cats.implicits._
import com.burgers.billing.models._
import com.burgers.billing.repos.{InvoiceRepository, UsageRepository}
import com.typesafe.scalalogging.StrictLogging

import java.time.LocalDate

trait UsageService[F[_]] {

  def create(usageInput: UsageInput): F[Int]

  def get(usageFilterInput: UsageFilterInput): F[Vector[Usage]]

  def generateInvoice(invoiceFilterInput: InvoiceFilterInput): F[Invoice]

  def getInvoice(invoiceId: Int): F[Invoice]

}

object UsageService {
  def build[F[_], G[_]: MonadThrow](
    usageRepository: UsageRepository[G],
    invoiceRepository: InvoiceRepository[G],
    gToF: G ~> F
  ): UsageService[F] =
    new UsageServiceImpl[F, G](usageRepository, invoiceRepository, gToF)

  private[services] val badAmountError = new Exception("amount must be greater than or equal to 0")
  private[services] def invoiceNotFoundError(invoiceId: Int) = new Exception(s"InvoiceId not found: ${invoiceId}")

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

class UsageServiceImpl[F[_], G[_]: MonadThrow](
  usageRepo: UsageRepository[G],
  invoiceRepo: InvoiceRepository[G],
  gToF: G ~> F
) extends UsageService[F] with StrictLogging {

  import UsageService._

  override def create(usageInput: UsageInput): F[Int] = {
    val res = for {
      date <- validateDate(usageInput.date).liftTo[G]
      usageUnits <- validateUsageUnits(usageInput.units).liftTo[G]
      amount <- validateAmount(usageInput).liftTo[G]
      resp <- usageRepo.create(date, usageUnits, amount)
    } yield resp.id

    gToF(res)
  }

  override def get(usageFilterInput: UsageFilterInput): F[Vector[Usage]] = {
    logger.info(s"get input: ${usageFilterInput}")
    val res = for {
      startDate <- usageFilterInput.startDate.map(validateDate).sequence.liftTo[G]
      endDate <- usageFilterInput.endDate.map(validateDate).sequence.liftTo[G]
      usageUnits <- usageFilterInput.usageUnits.map(validateUsageUnits).sequence.liftTo[G]
      resp <- usageRepo.get(
        idFilter = usageFilterInput.id,
        startDateFilter = startDate,
        endDateFilter = endDate,
        usageUnitsFilter = usageUnits,
        invoicedFilter = usageFilterInput.isInvoiced
      )
    } yield resp

    gToF(res)
  }

  private def generateInvoiceItems(usages: Vector[Usage]): Vector[InvoiceItem] = {
    usages.groupBy(_.units).flatMap { case (units, usagesByUnits) =>
      usagesByUnits.groupBy(_.date).map { case (date, groupedUsages) =>
        val amountTotal = groupedUsages.map(_.amount).sum
        InvoiceItem(
          date = date,
          units = units,
          amount = amountTotal,
          totalCost = amountTotal * units.rate
        )
      }
    }.toVector
  }

  private def buildInvoice(usages: Vector[Usage], invoiceId: Int, invoiceDate: LocalDate): Invoice = {
    val items = generateInvoiceItems(usages)
    val invoiceTotal = items.map(_.totalCost).sum
    Invoice(
      id = invoiceId,
      invoiceDate = invoiceDate,
      total = invoiceTotal,
      invoiceItems = items
    )
  }

  private def getInvoiceDate(invoiceId: Int): G[LocalDate] = for {
    invoiceDateOpt <- invoiceRepo.getInvoiceDate(invoiceId)
    invoiceDate <- invoiceDateOpt.toRight(invoiceNotFoundError(invoiceId)).liftTo[G]
  } yield invoiceDate

  override def generateInvoice(invoiceFilterInput: InvoiceFilterInput): F[Invoice] = {
    val res = for {
      startDate <- invoiceFilterInput.startDate.map(validateDate).sequence.liftTo[G]
      endDate <- invoiceFilterInput.endDate.map(validateDate).sequence.liftTo[G]
      usageUnits <- invoiceFilterInput.usageUnits.map(validateUsageUnits).sequence.liftTo[G]
      usageForInvoicing <- usageRepo.get(
        idFilter = None,
        startDateFilter = startDate,
        endDateFilter = endDate,
        usageUnitsFilter = usageUnits,
        invoicedFilter = Some(false)
      )
      invoiceId <- invoiceRepo.createInvoice(LocalDate.now())
      invoiceDate <- getInvoiceDate(invoiceId)
      _ <- usageRepo.updateUsageWithInvoice(usageForInvoicing.map(_.id), invoiceId)
    } yield buildInvoice(usageForInvoicing, invoiceId, invoiceDate)

    gToF(res)
  }

  override def getInvoice(invoiceId: Int): F[Invoice] = {
    val res = for {
      invoicedUsage <- usageRepo.getByInvoiceId(invoiceId)
      invoiceDate <- getInvoiceDate(invoiceId)
    } yield buildInvoice(invoicedUsage, invoiceId, invoiceDate)

    gToF(res)
  }

}
