package com.local.quickstart.domain.course

import cats._
import cats.data.EitherT
import cats.implicits._
import com.local.quickstart.domain.InvalidModelError
import com.local.quickstart.infrastructure.util.validation.Check._
import com.local.quickstart.infrastructure.util.validation.Check.CheckOps._

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
class CourseValidationInterpreter[F[_]: Monad](accountRepo: CourseRepositoryAlgebra[F])
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
  def apply[F[_]: Monad](repo: CourseRepositoryAlgebra[F]): CourseValidationInterpreter[F] =
    new CourseValidationInterpreter[F](repo)

  private val checkName = checkPred(longerThan(3)("Name") and alpha)


  def checkModel(course: Course): Either[Errors, Course] =
    (Either.right(course.id),
      checkName(course.name)).mapN(Course)
}



