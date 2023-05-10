package com.burgers.billing.servers

import cats.effect.Concurrent
import cats.implicits._
import com.burgers.billing.models.{InvoiceFilterInput, UsageFilterInput, UsageInput}
import com.burgers.billing.models.Usage._
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
            resp <- Ok(result)
          } yield resp
          response.handleErrorWith {
            e: Throwable => BadRequest(e.getMessage)
          }
        }

      case req @ POST -> Root / "usage" / "get" =>
        req.decode[UsageFilterInput] { usageFilter =>
          val response = for {
            result <- usageService.get(usageFilter)
            resp <- Ok(result)
          } yield resp

          response.handleErrorWith {
            e: Throwable => BadRequest(e.getMessage)
          }
        }

      case req @ POST -> Root / "invoice" / "generate" =>
        req.decode[InvoiceFilterInput] { invoiceFilter =>
          val response = for {
            result <- usageService.generateInvoice(invoiceFilter)
            resp <- Ok(result)
          } yield resp

          response.handleErrorWith {
            e: Throwable => BadRequest(e.getMessage)
          }
        }

      case GET -> Root / "invoice" / invoiceId =>
        val response = for {
          result <- usageService.getInvoice(invoiceId)
          resp <- Ok(result)
        } yield resp

        response.handleErrorWith {
          e: Throwable => BadRequest(e.getMessage)
        }
    }
  }
}