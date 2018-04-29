package infrastructure.endpoint

import domain.{AlreadyExistsError, InvalidModelError}
import cats.effect.Effect
import cats.implicits._
import domain.account.Account
import domain.professor.{Professor, ProfessorService}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpService}

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 29/04/2018
  */
class ProfessorEndpoints[F[_]: Effect] extends Http4sDsl[F] {

  implicit val professorDecoder:     EntityDecoder[F, Professor]       = jsonOf[F, Professor]
  implicit val professorListDecoder: EntityDecoder[F, List[Professor]] = jsonOf[F, List[Professor]]
  implicit val accountDecoder:       EntityDecoder[F, Account]         = jsonOf[F, Account]

  private def createUser(professorService: ProfessorService[F]): HttpService[F] =
    HttpService[F] {
      case req @ POST -> Root / "professor" =>
        val action = for {
          account <- req.as[Professor]
          result  <- professorService.create(account).value
        } yield result

        action.flatMap {
          case Right(saved) => Ok(saved.asJson)
          case Left(error) =>
            error match {
              case AlreadyExistsError(existing) =>
                Conflict(
                  s"The user with the email ${existing.asInstanceOf[Professor].account.get.email} already exists"
                )
              case InvalidModelError(errors) =>
                Conflict(s"The following errors have occurred when trying to save: ${errors
                  .mkString(", ")}")
            }

        }
    }

  private def getUsers(professorService: ProfessorService[F]): HttpService[F] =
    HttpService[F] {
      case GET -> Root / "professor" =>
        professorService.getAll.value.flatMap {
          case Right(result) => Ok(result.asJson)
          case Left(e) =>
            InternalServerError(s"An error occurred while trying to retrieve the data: $e")
        }
    }

  def endpoints(professorService: ProfessorService[F]): HttpService[F] =
    createUser(professorService) <+>
      getUsers(professorService)
}

object ProfessorEndpoints {

  def apply[F[_]: Effect](professorService: ProfessorService[F]): HttpService[F] =
    new ProfessorEndpoints[F].endpoints(professorService)
}
