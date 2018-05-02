package domain.assignment

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

  private val checkName = checkPred(longerThan(3)("Name") and alphanumeric)

  def checkModel(assignment: Assignment): Either[Errors, Assignment] =
    /*_*/
    (
      Either.right(assignment.id),
      Either.right(assignment.course),
      checkName(assignment.name)
    ).mapN(Assignment)
}
