package com.local.quickstart.infrastructure.endpoint

import com.local.quickstart.domain.AccountAlreadyExistsError
import com.local.quickstart.domain.account.{Account, AccountService}
import cats.effect.Effect
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpService}

import scala.language.higherKinds

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
class AccountEndpoints[F[_]: Effect] extends Http4sDsl[F] {

  implicit val accountDecoder: EntityDecoder[F, Account] = jsonOf[F, Account]

  private def createUser(accountService: AccountService[F]): HttpService[F] =
    HttpService[F] {
      case req @ POST -> Root / "account" =>
        val action = for {
          account <- req.as[Account]
          result  <- accountService.create(account).value
        } yield result

        action.flatMap {
          case Right(saved) => Ok(saved.asJson)
          case Left(AccountAlreadyExistsError(existing)) =>
            Conflict(
              s"The user with user name ${existing.email} already exists")

        }
    }

  def endpoints(accountService: AccountService[F]): HttpService[F] =
    createUser(accountService)
}

object AccountEndpoints {
  def apply[F[_]: Effect](accountService: AccountService[F]): HttpService[F] =
    new AccountEndpoints[F].endpoints(accountService)
}
