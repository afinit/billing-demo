package com.burgers.billing.repos

import cats.effect.IO
import com.burgers.billing.models.{Usage, UsageUnits}
import doobie.free.connection.unit
import doobie.implicits._
import doobie.util.transactor.{Strategy, Transactor}
import munit.CatsEffectSuite

import java.time.LocalDate

class UsageRepositorySpec extends CatsEffectSuite {

  private val invoiceId = 1

  private val usage1 = Usage(
    id = 1,
    date = LocalDate.of(2023, 5, 10),
    units = UsageUnits.StorageBytes,
    amount = 32,
    invoiceId = None
  )

  private val usage2 = Usage(
    id = 2,
    date = LocalDate.of(2023, 6, 9),
    units = UsageUnits.Cpu,
    amount = 41,
    invoiceId = None
  )

  private val usage3 = Usage(
    id = 3,
    date = LocalDate.of(2023, 7, 8),
    units = UsageUnits.BandwidthBytes,
    amount = 50,
    invoiceId = Some(invoiceId)
  )

  // create in memory db for testing
  private val transactorBase = Transactor.fromDriverManager[IO](
    "org.sqlite.JDBC", // driver classname
    "jdbc:sqlite::memory:", // connect URL
    "",
    "",
  )

  // prevents commits to the db so we can always work with a clean db
  private val transactor = Transactor.strategy.set(transactorBase, Strategy.default.copy(after = unit, oops = unit))

  test("testing the sqlite usage repo") {
    val actual = for {
      _ <- SqliteDbSetup.createTables
      usageRepo = UsageRepository.buildSqlite
      invoiceRepo = InvoiceRepository.buildSqlite

      _ <- usageRepo.create(usage1.date, usage1.units, usage1.amount)
      _ <- usageRepo.create(usage2.date, usage2.units, usage2.amount)
      _ <- usageRepo.create(usage3.date, usage3.units, usage3.amount)
      invoiceId <- invoiceRepo.createInvoice(LocalDate.now())
      _ <- usageRepo.updateUsageWithInvoice(Vector(3), invoiceId)
      allUsages <- usageRepo.get(None, None, None, None, None)
      invoicedUsage <- usageRepo.getByInvoiceId(invoiceId)
    } yield (allUsages, invoicedUsage)

    val expected = (Vector(usage1, usage2, usage3), Vector(usage3))

    val actualIO = actual.transact(transactor)
    assertIO(actualIO, expected)
  }

}