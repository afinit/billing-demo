package com.burgers.billing.services

import cats.implicits.catsSyntaxEitherId
import com.burgers.billing.models.{InputDate, InputUsageUnits, UsageInput, UsageUnits}
import munit.CatsEffectSuite

import java.time.LocalDate

class UsageServiceSpec extends CatsEffectSuite {
  import UsageService._

  private val badInput = UsageInput(
    date = InputDate("211-1-1-1"),
    units = InputUsageUnits("badunits"),
    amount = -21
  )

  private val goodInput = UsageInput(
    date = InputDate("2023-05-10"),
    units = InputUsageUnits("Storagebytes"),
    amount = 23
  )

  test("UsageService validates date") {

    val expectedBadDateResult = "Text '211-1-1-1' could not be parsed at index 0".asLeft[LocalDate]
    val expectedGoodDateResult = Right(LocalDate.of(2023, 5, 10))

    val actualBadDateResult = validateDate(badInput.date).left.map(_.getMessage)
    val actualGoodDateResult = validateDate(goodInput.date)

    assertEquals(actualBadDateResult, expectedBadDateResult)
    assertEquals(actualGoodDateResult, expectedGoodDateResult)
  }

  test("UsageService validates usage units") {
    val expectedBad = UsageUnits.badUnitsError.asLeft[UsageUnits]
    val expectedGood = UsageUnits.StorageBytes.asRight[Exception]

    val actualBad = UsageService.validateUsageUnits(badInput.units)
    val actualGood = UsageService.validateUsageUnits(goodInput.units)

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