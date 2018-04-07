package com.local.quickstart.infrastructure.endpoint

import cats.effect.Effect
import io.circe._
import org.http4s.circe._
import org.http4s.HttpService
import org.http4s.dsl.Http4sDsl

import scala.language.higherKinds

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
class TestEndpoint[F[_]: Effect]() extends Http4sDsl[F] {

  def service: HttpService[F] = HttpService[F] {
    case GET -> Root / "hello" / name =>
      Ok(Json.obj("message" -> Json.fromString(s"Hello, $name")))
  }
}

object TestEndpoint {
  def apply[F[_]: Effect]: HttpService[F] =
    new TestEndpoint[F].service
}
