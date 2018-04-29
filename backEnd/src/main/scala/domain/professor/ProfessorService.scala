package domain.professor

import cats.Monad
import cats.data.EitherT
import domain.{GenericService, ValidationError}

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 29/04/2018
  */
class ProfessorService[F[_]](professorRepo: ProfessorRepositoryAlgebra[F], validation: ProfessorValidationAlgebra[F])
    extends GenericService[F, Professor] {

  override def create(o: Professor)(implicit M: Monad[F]): EitherT[F, ValidationError, Professor] =
    for {
      _     <- validation.checkModel(o)
      saved <- EitherT.liftF(professorRepo.create(o))
    } yield saved

  override def getAll(implicit M: Monad[F]): EitherT[F, _, List[Professor]] =
    EitherT.liftF(professorRepo.getAll)
}

object ProfessorService {

  def apply[F[_]](
    repositoryAlgebra: ProfessorRepositoryAlgebra[F],
    validationAlgebra: ProfessorValidationAlgebra[F]
  ): ProfessorService[F] =
    new ProfessorService[F](repositoryAlgebra, validationAlgebra)
}
