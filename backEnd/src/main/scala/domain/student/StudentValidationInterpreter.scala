package domain.student

import cats._
import cats.data.EitherT
import cats.implicits._
import domain.InvalidModelError
import util.Check._
import util.Check.CheckOps._

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 30/04/2018
  */
class StudentValidationInterpreter[F[_]: Monad](professorRepo: StudentRepositoryAlgebra[F])
    extends StudentValidationAlgebra[F] {

  override def checkModel(student: Student) =
    EitherT.fromEither {
      StudentValidationInterpreter.checkModel(student) match {
        case Left(value) => Left(InvalidModelError(value.toList))
        case Right(_)    => Right(())
      }
    }
}

object StudentValidationInterpreter {

  def apply[F[_]: Monad](repo: StudentRepositoryAlgebra[F]): StudentValidationInterpreter[F] =
    new StudentValidationInterpreter[F](repo)

  private val checkName = checkPred(longerThan(1)("Title") and alphanumeric)

  def checkModel(student: Student): Either[Errors, Student] =
    /*_*/
    (
      Either.right(student.id),
      Either.right(student.account)
    ).mapN(Student)
}
