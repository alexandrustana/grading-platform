package domain.student

import cats.Monad
import cats.data.EitherT
import domain.{GenericService, ValidationError}

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 30/04/2018
  */
class StudentService[F[_]](professorRepo: StudentRepositoryAlgebra[F], validation: StudentValidationAlgebra[F])
    extends GenericService[F, Student] {

  override def create(o: Student)(implicit M: Monad[F]): EitherT[F, ValidationError, Student] =
    for {
      _     <- validation.checkModel(o)
      saved <- EitherT.liftF(professorRepo.create(o))
    } yield saved

  override def getAll(implicit M: Monad[F]): EitherT[F, _, List[Student]] =
    EitherT.liftF(professorRepo.getAll)
}

object StudentService {

  def apply[F[_]](
    repositoryAlgebra: StudentRepositoryAlgebra[F],
    validationAlgebra: StudentValidationAlgebra[F]
  ): StudentService[F] =
    new StudentService[F](repositoryAlgebra, validationAlgebra)
}
