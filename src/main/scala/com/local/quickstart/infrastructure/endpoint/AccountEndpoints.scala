package com.local.quickstart.infrastructure.endpoint

import com.local.quickstart.domain.{AlreadyExistsError, InvalidModelError}
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

  implicit val accountDecoder: EntityDecoder[F, Account]           = jsonOf[F, Account]
  implicit val accountListDecoder: EntityDecoder[F, List[Account]] = jsonOf[F, List[Account]]

  private def createUser(accountService: AccountService[F]): HttpService[F] =
    HttpService[F] {
      case req @ POST -> Root / "account" =>
        val action = for {
          account <- req.as[Account]
          result  <- accountService.create(account).value
        } yield result

        action.flatMap {
          case Right(saved) => Ok(saved.asJson)
          case Left(error) =>
            error match {
              case AlreadyExistsError(existing) =>
                Conflict(s"The user with user name ${existing.email} already exists")
              case InvalidModelError(errors) =>
                Conflict(s"The following errors have occurred when trying to save: ${errors.mkString(", ")}")
            }

        }
    }

  private def getUsers(accountService: AccountService[F]): HttpService[F] =
    HttpService[F] {
      case GET -> Root / "account" =>
        accountService.getAll.value.flatMap {
          case Right(result) => Ok(result.asJson)
          case Left(e) =>
            InternalServerError(s"An error occurred while trying to retrieve the data: $e")
        }
    }

  def endpoints(accountService: AccountService[F]): HttpService[F] =
    createUser(accountService) <+>
      getUsers(accountService)
}

object AccountEndpoints {
  def apply[F[_]: Effect](accountService: AccountService[F]): HttpService[F] =
    new AccountEndpoints[F].endpoints(accountService)
}
