package infrastructure.endpoint

import cats.effect.Effect
import cats.implicits._
import domain.submission.{Submission, SubmissionService}
import domain.InvalidModelError
import domain.assignment.Assignment
import domain.student.Student
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpService}

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 03/05/2018
  */
class SubmissionEndpoints[F[_]: Effect] extends Http4sDsl[F] {

  implicit val submissionDecoder:     EntityDecoder[F, Submission]       = jsonOf[F, Submission]
  implicit val submissionListDecoder: EntityDecoder[F, List[Submission]] = jsonOf[F, List[Submission]]
  implicit val studentDecoder:        EntityDecoder[F, Student]          = jsonOf[F, Student]
  implicit val assignmentDecoder:     EntityDecoder[F, Assignment]       = jsonOf[F, Assignment]

  private def create(submissionService: SubmissionService[F]): HttpService[F] =
    HttpService[F] {
      case req @ POST -> Root / "submission" =>
        val action = for {
          account <- req.as[Submission]
          result  <- submissionService.create(account).value
        } yield result

        action.flatMap {
          case Right(saved) => Ok(saved.asJson)
          case Left(error) =>
            error match {
              case InvalidModelError(errors) =>
                Conflict(s"The following errors have occurred when trying to save: ${errors
                  .mkString(", ")}")
            }

        }
    }

  private def getAll(submissionService: SubmissionService[F]): HttpService[F] =
    HttpService[F] {
      case GET -> Root / "submission" =>
        submissionService.getAll.value.flatMap {
          case Right(result) => Ok(result.asJson)
          case Left(e) =>
            InternalServerError(s"An error occurred while trying to retrieve the data: $e")
        }
    }

  def endpoints(submissionService: SubmissionService[F]): HttpService[F] =
    create(submissionService) <+>
      getAll(submissionService)
}

object SubmissionEndpoints {

  def apply[F[_]: Effect](submissionService: SubmissionService[F]): HttpService[F] =
    new SubmissionEndpoints[F].endpoints(submissionService)
}
