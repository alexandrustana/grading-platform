package com.local.quickstart.domain.assignment

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
class AssignmentValidationInterpreter[F[_]: Monad](accountRepo: AssignmentRepositoryAlgebra[F])
    extends AssignmentValidationAlgebra[F] {

  override def checkModel(assignment: Assignment) =
    EitherT.fromEither {
      AssignmentValidationInterpreter.checkModel(assignment) match {
        case Left(value) => Left(InvalidModelError(value.toList))
        case Right(_)    => Right(())
      }
    }
}

object AssignmentValidationInterpreter {
  def apply[F[_]: Monad](repo: AssignmentRepositoryAlgebra[F]): AssignmentValidationInterpreter[F] =
    new AssignmentValidationInterpreter[F](repo)

  import com.local.quickstart.domain.course.CourseValidationInterpreter

  private val checkName = checkPred(longerThan(3)("Name") and alphanumeric)

  def checkModel(assignment: Assignment): Either[Errors, Assignment] =
    (Either.right(assignment.id),
      checkName(assignment.name),
      CourseValidationInterpreter.checkModel(assignment.course)).mapN(Assignment)
}



