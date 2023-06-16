package com.burgers.billing

import cats.arrow.FunctionK
import cats.effect.kernel.Resource
import cats.effect.{IO, IOApp}
import cats.~>
import com.burgers.billing.repos.{InvoiceRepository, SqliteDbSetup, UsageRepository}
import com.burgers.billing.servers.BillingServer
import com.burgers.billing.services.UsageService
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor

object Main extends IOApp.Simple {
  private val transactor = Transactor.fromDriverManager[IO](
    "org.sqlite.JDBC", "jdbc:sqlite:billing.db", "", ""
  )
  private val gToF: ConnectionIO ~> IO = new FunctionK[ConnectionIO, IO] {
    override def apply[A](fa: ConnectionIO[A]): IO[A] = fa.transact(transactor)
  }

  private val resource: Resource[IO, Unit] = for {
    _ <- Resource.eval(gToF(SqliteDbSetup.createTables))

    usageRepo = UsageRepository.buildSqlite
    invoiceRepo = InvoiceRepository.buildSqlite
    usageService = UsageService.build[IO, ConnectionIO](usageRepo, invoiceRepo, gToF)

    server <- BillingServer.build[IO](usageService)
  } yield server

  val run = resource.useForever
}
