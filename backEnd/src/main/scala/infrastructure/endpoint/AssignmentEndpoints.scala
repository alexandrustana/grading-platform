package infrastructure.endpoint

import cats.effect.Effect
import cats.implicits._
import domain.course.Course
import domain.assignment.{Assignment, AssignmentService}
import domain.InvalidModelError
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpService}

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
class AssignmentEndpoints[F[_]: Effect] extends Http4sDsl[F] {

  implicit val assignmentDecoder:     EntityDecoder[F, Assignment]       = jsonOf[F, Assignment]
  implicit val assignmentListDecoder: EntityDecoder[F, List[Assignment]] = jsonOf[F, List[Assignment]]
  implicit val courseDecoder:         EntityDecoder[F, Course]           = jsonOf[F, Course]

  private def create(assignmentService: AssignmentService[F]): HttpService[F] =
    HttpService[F] {
      case req @ POST -> Root / "assignment" =>
        val action = for {
          assignment <- req.as[Assignment]
          result     <- assignmentService.create(assignment).value
        } yield result

        action.flatMap {
          case Right(saved) => Ok(saved.asJson)
          case Left(error) =>
            error match {
              case InvalidModelError(errors) =>
                Conflict(s"The following errors have occurred when trying to save: ${errors
                  .mkString(", ")}")
              case _ => InternalServerError("An internal error has occurred")
            }

        }
    }

  private def getAll(assignmentService: AssignmentService[F]): HttpService[F] =
    HttpService[F] {
      case GET -> Root / "assignment" =>
        assignmentService.getAll.value.flatMap {
          case Right(result) => Ok(result.asJson)
          case Left(e) =>
            InternalServerError(s"An error occurred while trying to retrieve the data: $e")
        }
    }

  def endpoints(assignmentService: AssignmentService[F]): HttpService[F] =
    create(assignmentService) <+>
      getAll(assignmentService)
}

object AssignmentEndpoints {

  def apply[F[_]: Effect](assignmentService: AssignmentService[F]): HttpService[F] =
    new AssignmentEndpoints[F].endpoints(assignmentService)
}
