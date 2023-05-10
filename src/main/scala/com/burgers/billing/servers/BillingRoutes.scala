package com.burgers.billing.servers

import cats.effect.Concurrent
import cats.implicits._
import com.burgers.billing.models.UsageInput
import com.burgers.billing.models.UsageInput._
import com.burgers.billing.services.UsageService
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object BillingRoutes {

  def usageRoutes[F[_]: Concurrent](usageService: UsageService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req @ POST -> Root / "usage" / "create" =>
        req.decode[UsageInput] { usage =>
          val response = for {
            result <- usageService.create(usage)
            resp <- Ok(result.toString)
          } yield resp
          response.handleErrorWith {
            e: Throwable => BadRequest(e.getMessage)
          }
        }
    }
  }
}