package domain.professor

import cats._
import cats.data.EitherT
import cats.implicits._
import domain.InvalidModelError
import util.Check._
import util.Check.CheckOps._

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 29/04/2018
  */
class ProfessorValidationInterpreter[F[_]: Monad](professorRepo: ProfessorRepositoryAlgebra[F])
    extends ProfessorValidationAlgebra[F] {

  override def checkModel(professor: Professor) =
    EitherT.fromEither {
      ProfessorValidationInterpreter.checkModel(professor) match {
        case Left(value) => Left(InvalidModelError(value.toList))
        case Right(_)    => Right(())
      }
    }
}

object ProfessorValidationInterpreter {

  def apply[F[_]: Monad](repo: ProfessorRepositoryAlgebra[F]): ProfessorValidationInterpreter[F] =
    new ProfessorValidationInterpreter[F](repo)

  private val checkName = checkPred(longerThan(1)("Title") and alphanumeric)

  def checkModel(professor: Professor): Either[Errors, Professor] =
    /*_*/
    (
      Either.right(professor.id),
      Either.right(professor.account),
      checkName(professor.title)
    ).mapN(Professor)
}
