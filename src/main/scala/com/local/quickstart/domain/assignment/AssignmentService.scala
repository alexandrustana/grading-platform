package com.local.quickstart.domain.assignment

import cats.Monad
import cats.data.EitherT
import com.local.quickstart.domain.{GenericService, ValidationError}

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
class AssignmentService[F[_]](accountRepo: AssignmentRepositoryAlgebra[F],
                           validation: AssignmentValidationAlgebra[F])
    extends GenericService[F, Assignment] {

  override def create(o: Assignment)(implicit M: Monad[F]): EitherT[F, ValidationError, Assignment] =
    for {
      _     <- validation.checkModel(o)
      saved <- EitherT.liftF(accountRepo.create(o))
    } yield saved

  override def getAll(implicit M: Monad[F]): EitherT[F, _, List[Assignment]] =
    EitherT.liftF(accountRepo.getAll)
}

object AssignmentService {
  def apply[F[_]](repositoryAlgebra: AssignmentRepositoryAlgebra[F],
                  validationAlgebra: AssignmentValidationAlgebra[F]): AssignmentService[F] =
    new AssignmentService[F](repositoryAlgebra, validationAlgebra)
}
