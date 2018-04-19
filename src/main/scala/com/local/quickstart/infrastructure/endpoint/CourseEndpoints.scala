package com.local.quickstart.infrastructure.endpoint

import cats.effect.Effect
import cats.implicits._
import com.local.quickstart.domain.course.{Course, CourseService}
import com.local.quickstart.domain.InvalidModelError
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
class CourseEndpoints[F[_]: Effect] extends Http4sDsl[F] {

  implicit val courseDecoder: EntityDecoder[F, Course]           = jsonOf[F, Course]
  implicit val courseListDecoder: EntityDecoder[F, List[Course]] = jsonOf[F, List[Course]]

  private def createCourse(courseService: CourseService[F]): HttpService[F] =
    HttpService[F] {
      case req @ POST -> Root / "course" =>
        val action = for {
          course <- req.as[Course]
          result  <- courseService.create(course).value
        } yield result

        action.flatMap {
          case Right(saved) => Ok(saved.asJson)
          case Left(error) =>
            error match {
              case InvalidModelError(errors) =>
                Conflict(s"The following errors have occurred when trying to save: ${errors.mkString(", ")}")
              case _ => InternalServerError("An internal error has occurred")
            }

        }
    }

  private def getCourses(courseService: CourseService[F]): HttpService[F] =
    HttpService[F] {
      case GET -> Root / "course" =>
        courseService.getAll.value.flatMap {
          case Right(result) => Ok(result.asJson)
          case Left(e) =>
            InternalServerError(s"An error occurred while trying to retrieve the data: $e")
        }
    }

  def endpoints(courseService: CourseService[F]): HttpService[F] =
    createCourse(courseService) <+>
      getCourses(courseService)
}

object CourseEndpoints {
  def apply[F[_]: Effect](courseService: CourseService[F]): HttpService[F] =
    new CourseEndpoints[F].endpoints(courseService)
}
