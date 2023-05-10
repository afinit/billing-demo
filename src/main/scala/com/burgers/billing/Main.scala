package com.burgers.billing

import cats.effect.{IO, IOApp}
import com.burgers.billing.servers.BillingServer

object Main extends IOApp.Simple {
  val run = BillingServer.run[IO]
}
