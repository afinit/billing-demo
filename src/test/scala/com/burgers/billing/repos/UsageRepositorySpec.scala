package com.burgers.billing.repos

import cats.effect.IO
import com.burgers.billing.models.{Usage, UsageUnits}
import munit.CatsEffectSuite

import java.time.LocalDate

class UsageRepositorySpec extends CatsEffectSuite {

  import UsageRepository._

  private val invoiceId = "Burger of the day"

  private val usage1 = Usage(
    id = "1",
    date = LocalDate.of(2023, 5, 10),
    units = UsageUnits.StorageBytes,
    amount = 32,
    invoiceId = None
  )

  private val usage2 = Usage(
    id = "2",
    date = LocalDate.of(2023, 6, 9),
    units = UsageUnits.Cpu,
    amount = 41,
    invoiceId = None
  )

  private val usage3 = Usage(
    id = "3",
    date = LocalDate.of(2023, 7, 8),
    units = UsageUnits.BandwidthBytes,
    amount = 50,
    invoiceId = Some(invoiceId)
  )

  test("UsageRepository filters by id") {
    val filter = filterById(Some("1"), _)
    val filterEmpty = filterById(None, _)

    val actual1 = filter(usage1)
    val actual2 = filter(usage2)
    val actual3 = filter(usage3)
    val actual4 = filterEmpty(usage1)

    assertEquals(actual1, true)
    assertEquals(actual2, false)
    assertEquals(actual3, false)
    assertEquals(actual4, true)
  }

  test("UsageRepository filters by startDate") {
    val filter = filterByStartDate(Some(LocalDate.of(2023, 6, 1)), _)
    val filterEmpty = filterByStartDate(None, _)

    val actual1 = filter(usage1)
    val actual2 = filter(usage2)
    val actual3 = filter(usage3)
    val actual4 = filterEmpty(usage1)

    assertEquals(actual1, false)
    assertEquals(actual2, true)
    assertEquals(actual3, true)
    assertEquals(actual4, true)
  }

  test("UsageRepository filters by endDate") {
    val filter = filterByEndDate(Some(LocalDate.of(2023, 6, 30)), _)
    val filterEmpty = filterByEndDate(None, _)

    val actual1 = filter(usage1)
    val actual2 = filter(usage2)
    val actual3 = filter(usage3)
    val actual4 = filterEmpty(usage1)

    assertEquals(actual1, true)
    assertEquals(actual2, true)
    assertEquals(actual3, false)
    assertEquals(actual4, true)
  }

  test("UsageRepository filters by usage units") {
    val filter = filterByUsageUnits(Some(UsageUnits.Cpu), _)
    val filterEmpty = filterByUsageUnits(None, _)

    val actual1 = filter(usage1)
    val actual2 = filter(usage2)
    val actual3 = filter(usage3)
    val actual4 = filterEmpty(usage1)

    assertEquals(actual1, false)
    assertEquals(actual2, true)
    assertEquals(actual3, false)
    assertEquals(actual4, true)
  }

  test("UsageRepository should properly use each of its functions") {
    val usageRepo = UsageRepository.build[IO]

    val expected = (Vector(usage1, usage2, usage3), Vector(usage3))
    val actual = for {
      _ <- usageRepo.create(usage1.date, usage1.units, usage1.amount)
      _ <- usageRepo.create(usage2.date, usage2.units, usage2.amount)
      _ <- usageRepo.create(usage3.date, usage3.units, usage3.amount)
      _ <- usageRepo.updateUsageWithInvoice(Vector("3"), invoiceId)
      allUsages <- usageRepo.get(None, None, None, None, None)
      invoicedUsage <- usageRepo.getByInvoiceId(invoiceId)
    } yield (allUsages, invoicedUsage)

    assertIO(actual, expected)
  }

}