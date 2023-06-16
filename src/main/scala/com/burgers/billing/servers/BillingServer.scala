package com.burgers.billing.servers

import cats.effect.Async
import cats.effect.kernel.Resource
import com.burgers.billing.repos.{UsageRepository, UsageRepositorySqlite}
import com.burgers.billing.services.UsageService
import com.comcast.ip4s._
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object BillingServer {

  def run[F[_]: Async]: F[Nothing] = {
    val transactor = Transactor.fromDriverManager[F](
      "org.sqlite.JDBC", "jdbc:sqlite:billing.db", "", ""
    )
    val usageRepo = UsageRepository.build[F]
    val usageService = UsageService.build[F](usageRepo)

    val httpApp = (
        BillingRoutes.usageRoutes(usageService)
      ).orNotFound

    // With Middlewares in place
    val finalHttpApp = Logger.httpApp(true, true)(httpApp)

    for {
      _ <- Resource.eval(UsageRepositorySqlite.build.transact(transactor))
      _ <-
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
}
