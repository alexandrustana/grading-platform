package domain.course

import cats._
import cats.data.EitherT
import cats.implicits._
import domain.InvalidModelError
import util.Check._
import util.Check.CheckOps._

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
class CourseValidationInterpreter[F[_]: Monad](
    accountRepo: CourseRepositoryAlgebra[F])
    extends CourseValidationAlgebra[F] {

  override def checkModel(course: Course) =
    EitherT.fromEither {
      CourseValidationInterpreter.checkModel(course) match {
        case Left(value) => Left(InvalidModelError(value.toList))
        case Right(_)    => Right(())
      }
    }
}

object CourseValidationInterpreter {
  def apply[F[_]: Monad](
      repo: CourseRepositoryAlgebra[F]): CourseValidationInterpreter[F] =
    new CourseValidationInterpreter[F](repo)

  private val checkName = checkPred(longerThan(3)("Name") and alphanumeric)

  def checkModel(course: Course): Either[Errors, Course] =
    (Either.right(course.id), checkName(course.name)).mapN(Course)
}
