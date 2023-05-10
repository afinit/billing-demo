package com.burgers.billing.services

import cats.implicits.catsSyntaxEitherId
import com.burgers.billing.models.{UsageInput, UsageUnits}
import munit.CatsEffectSuite

import java.time.LocalDate

class UsageServiceSpec extends CatsEffectSuite {
  import UsageService._

  private val badInput = UsageInput(
    date = "211-1-1-1",
    units = "badunits",
    amount = -21
  )

  private val goodInput = UsageInput(
    date = "2023-05-10",
    units = "Storagebytes",
    amount = 23
  )

  test("UsageService validates date") {

    val expectedBadDateResult = "Text '211-1-1-1' could not be parsed at index 0".asLeft[LocalDate]
    val expectedGoodDateResult = Right(LocalDate.of(2023, 5, 10))

    val actualBadDateResult = validateDate(badInput).left.map(_.getMessage)
    val actualGoodDateResult = validateDate(goodInput)

    assertEquals(actualBadDateResult, expectedBadDateResult)
    assertEquals(actualGoodDateResult, expectedGoodDateResult)
  }

  test("UsageService validates usage units") {
    val expectedBad = UsageUnits.badUnitsError.asLeft[UsageUnits]
    val expectedGood = UsageUnits.StorageBytes.asRight[Exception]

    val actualBad = UsageService.validateUsageUnits(badInput)
    val actualGood = UsageService.validateUsageUnits(goodInput)

    assertEquals(actualBad, expectedBad)
    assertEquals(actualGood, expectedGood)
  }

  test("UsageService validates usage amount") {
    val expectedBad = UsageService.badAmountError.asLeft[BigDecimal]
    val expectedGood = goodInput.amount.asRight[Exception]

    val actualBad = UsageService.validateAmount(badInput)
    val actualGood = UsageService.validateAmount(goodInput)

    assertEquals(actualBad, expectedBad)
    assertEquals(actualGood, expectedGood)
  }

}