package com.burgers.billing.servers

import cats.effect.Async
import com.burgers.billing.services.UsageService
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object BillingServer {

  def run[F[_]: Async]: F[Nothing] = {
    val usageService = UsageService.build[F]

    val httpApp = (
        BillingRoutes.usageRoutes(usageService)
      ).orNotFound

    // With Middlewares in place
    val finalHttpApp = Logger.httpApp(true, true)(httpApp)

    for {
      _ <-
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
}
