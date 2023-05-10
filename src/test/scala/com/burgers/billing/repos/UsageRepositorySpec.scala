package com.burgers.billing.repos

import com.burgers.billing.models.{Usage, UsageUnits}
import munit.CatsEffectSuite

import java.time.LocalDate

class UsageRepositorySpec extends CatsEffectSuite {

  import UsageRepository._

  private val usage1 = Usage(
    id = "bob",
    date = LocalDate.of(2023, 5, 10),
    units = UsageUnits.StorageBytes,
    amount = 32
  )

  private val usage2 = Usage(
    id = "louise",
    date = LocalDate.of(2023, 6, 9),
    units = UsageUnits.Cpu,
    amount = 41
  )

  private val usage3 = Usage(
    id = "gene",
    date = LocalDate.of(2023, 7, 8),
    units = UsageUnits.BandwidthBytes,
    amount = 50
  )

  test("UsageRepository filters by id") {
    val filter = filterById(Some("bob"), _)
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

}