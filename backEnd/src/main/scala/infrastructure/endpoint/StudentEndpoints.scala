package infrastructure.endpoint

import domain.{AlreadyExistsError, InvalidModelError, ValidationError}
import cats.effect.Effect
import cats.implicits._
import domain.account.{Account, AccountService}
import domain.student.{Student, StudentService}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpService}

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 30/04/2018
  */
class StudentEndpoints[F[_]: Effect] extends Http4sDsl[F] {

  implicit val studentDecoder:     EntityDecoder[F, Student]       = jsonOf[F, Student]
  implicit val studentListDecoder: EntityDecoder[F, List[Student]] = jsonOf[F, List[Student]]
  implicit val accountDecoder:     EntityDecoder[F, Account]       = jsonOf[F, Account]

  private def create(studentService: StudentService[F], accountService: AccountService[F]): HttpService[F] =
    HttpService[F] {
      case req @ POST -> Root / "student" =>
        val action = for {
          student  <- req.as[Student]
          accountR <- accountService.create(student.account.get).value
          result <- accountR match {
                     case Left(v)  => Either.left[ValidationError, Student](v).pure[F]
                     case Right(v) => studentService.create(student.copy(account = Option(v))).value
                   }
        } yield result

        action.flatMap {
          case Right(saved) => Ok(saved.asJson)
          case Left(error) =>
            error match {
              case AlreadyExistsError(existing) =>
                Conflict(
                  s"The user with the email ${existing match { case s: Student => s.account.get.email }} already exists"
                )
              case InvalidModelError(errors) =>
                Conflict(s"The following errors have occurred when trying to save: ${errors
                  .mkString(", ")}")
              case _ => Conflict("A server error has occurred")
            }

        }
    }

  private def getAll(studentService: StudentService[F]): HttpService[F] =
    HttpService[F] {
      case GET -> Root / "student" =>
        studentService.getAll.value.flatMap {
          case Right(result) => Ok(result.asJson)
          case Left(e) =>
            InternalServerError(s"An error occurred while trying to retrieve the data: $e")
        }
    }

  def endpoints(studentService: StudentService[F], accountService: AccountService[F]): HttpService[F] =
  /*_*/
    create(studentService, accountService) <+>
      getAll(studentService)
  /*_*/
}

object StudentEndpoints {

  def apply[F[_]: Effect](studentService: StudentService[F], accountService: AccountService[F]): HttpService[F] =
    new StudentEndpoints[F].endpoints(studentService, accountService)
}
